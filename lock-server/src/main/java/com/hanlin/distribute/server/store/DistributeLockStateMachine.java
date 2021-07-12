package com.hanlin.distribute.server.store;

import cn.hutool.core.util.ObjectUtil;
import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Iterator;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.core.StateMachineAdapter;
import com.alipay.sofa.jraft.error.RaftError;
import com.alipay.sofa.jraft.error.RaftException;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotReader;
import com.alipay.sofa.jraft.storage.snapshot.SnapshotWriter;
import com.alipay.sofa.jraft.util.Utils;
import com.hanlin.distribute.client.command.BaseRequestCommand;
import com.hanlin.distribute.client.command.CommandType;
import com.hanlin.distribute.client.command.LockCommand;
import com.hanlin.distribute.client.command.ResponseCommand;
import com.hanlin.distribute.client.command.UnLockCommand;
import com.hanlin.distribute.server.closure.LeaderTaskClosure;
import com.hanlin.distribute.server.util.CommandCodec;
import com.hanlin.distribute.server.util.RecordLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author shaoyu
 * TODO: 可能存在数据过多问题，后期会是采用定时任务来定期清理过期 key 问题.
 */
public class DistributeLockStateMachine extends StateMachineAdapter {
    
    private static final Logger LOG = LoggerFactory.getLogger(DistributeLockStateMachine.class);
    
    private final Map<String, LockRecord> LOCK_KEY_MAP = new ConcurrentHashMap<String, LockRecord>();
    
    /**
     * 行级锁.
     */
    private final RecordLock recordLock = new RecordLock();
    
    /**
     * leader term
     */
    private final AtomicLong LEADER_TERM = new AtomicLong(-1);
    
    /**
     * 判断是否是 leader.
     *
     * @return
     */
    public boolean isLeader() {
        return this.LEADER_TERM.get() > 0;
    }
    
    private ResponseCommand lock(LockCommand command) {
        
        recordLock.lock(command.getLockKey());
        
        try {
            LockRecord lockRecord = LOCK_KEY_MAP.get(command.getLockKey());
            LockRecord newRecord = new LockRecord(command.getLockKey(),
                    System.currentTimeMillis() + command.getLockTime());
            if (null == lockRecord) {
                LockRecord oldRecord = LOCK_KEY_MAP.putIfAbsent(command.getLockKey(), newRecord);
                if (null == oldRecord) {
                    return new ResponseCommand(true);
                } else {
                    return new ResponseCommand(false, String.format("%s lock exist.", command.getLockKey()));
                }
            } else {
                Long lockTime = lockRecord.getLockTime();
                if (System.currentTimeMillis() > lockTime) {
                    boolean replace = LOCK_KEY_MAP.replace(lockRecord.getLockKey(), lockRecord, newRecord);
                    if (replace) {
                        return new ResponseCommand(true);
                    } else {
                        return new ResponseCommand(false, String.format("%s lock exist.", command.getLockKey()));
                    }
                }
            }
        } finally {
            recordLock.unlock(command.getLockKey());
        }
        
        return new ResponseCommand(false, "unknown error.");
    }
    
    private ResponseCommand unlock(UnLockCommand command) {
        recordLock.lock(command.getLockKey());
        try {
            LockRecord lockRecord = LOCK_KEY_MAP.get(command.getLockKey());
            if (null == lockRecord) {
                return new ResponseCommand(false,
                        String.format("%s lock does not exist, can not release.", command.getLockKey()));
            }
            LOCK_KEY_MAP.remove(command.getLockKey());
            return new ResponseCommand(true);
        } finally {
            recordLock.lock(command.getLockKey());
        }
    }
    
    @Override
    public void onApply(Iterator iter) {
        while (iter.hasNext()) {
            // 回调.
            final Closure done = iter.done();
            final ByteBuffer data = iter.getData();
            
            LeaderTaskClosure closure = null;
            CommandType commandType = null;
            BaseRequestCommand command = null;
            
            // 当本节点是 leader 时，直接从 closure 中获取值.
            // 否则解析 buffer.
            if (ObjectUtil.isNotEmpty(done)) {
                closure = (LeaderTaskClosure) done;
                commandType = closure.getCommandType();
                command = closure.getCommand();
            } else {
                final byte cmdType = data.get();
                final byte[] cmdRequestBytes = new byte[data.remaining()];
                data.get(cmdRequestBytes);
                commandType = CommandType.parseByte(cmdType);
                switch (commandType) {
                    case LOCK:
                        command = CommandCodec.decodeCommand(cmdRequestBytes, LockCommand.class);
                        break;
                    case UNLOCK:
                        command = CommandCodec.decodeCommand(cmdRequestBytes, UnLockCommand.class);
                        break;
                    default:
                }
            }
            
            String lockKey = command.getLockKey();
            ResponseCommand response = null;
            switch (commandType) {
                case LOCK:
                    response = lock((LockCommand) command);
                    break;
                case UNLOCK:
                    response = unlock((UnLockCommand) command);
                    break;
                default:
            }
            
            if (null != closure) {
                closure.setResponse(response);
                closure.run(Status.OK());
            }
            iter.next();
        }
    }
    
    @Override
    public void onSnapshotSave(SnapshotWriter writer, Closure done) {
        final Map<String, LockRecord> localMap = new HashMap<>();
        for (Map.Entry<String, LockRecord> entry : localMap.entrySet()) {
            localMap.put(entry.getKey(), entry.getValue());
        }
        Utils.runInThread(() -> {
            final LockSnapshotFile snapshot = new LockSnapshotFile(writer.getPath() + File.separator + "data");
            if (snapshot.save(localMap)) {
                if (writer.addFile("data")) {
                    done.run(Status.OK());
                } else {
                    done.run(new Status(RaftError.EIO, "Fail to add file to writer"));
                }
            } else {
                done.run(new Status(RaftError.EIO, "Fail to save counter snapshot %s", snapshot.getPath()));
            }
        });
    }
    
    @Override
    public boolean onSnapshotLoad(SnapshotReader reader) {
        if (isLeader()) {
            LOG.warn("Leader is not supposed to load snapshot");
            return false;
        }
        if (reader.getFileMeta("data") == null) {
            LOG.error("Fail to find data file in {}", reader.getPath());
            return false;
        }
        final LockSnapshotFile snapshot = new LockSnapshotFile(reader.getPath() + File.separator + "data");
        try {
            final Map<String, LockRecord> values = snapshot.load();
            this.LOCK_KEY_MAP.clear();
            if (values != null) {
                for (final Map.Entry<String, LockRecord> entry : values.entrySet()) {
                    LockCommand lockCommand = new LockCommand();
                    lockCommand.setLockKey(entry.getKey());
                    lockCommand.setLockTime(entry.getValue().getLockTime());
                    lock(lockCommand);
                }
            }
            return true;
        } catch (final IOException e) {
            LOG.error("Fail to load snapshot from {}", snapshot.getPath());
            return false;
        }
    }
    
    @Override
    public void onError(RaftException e) {
        LOG.error("Raft error: {}", e, e);
        super.onError(e);
    }
    
    @Override
    public void onLeaderStart(long term) {
        LEADER_TERM.set(term);
        super.onLeaderStart(term);
    }
    
    @Override
    public void onLeaderStop(Status status) {
        LEADER_TERM.set(-1L);
        super.onLeaderStop(status);
    }
}
