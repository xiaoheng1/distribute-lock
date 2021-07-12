package com.hanlin.distribute.server.processor;

import com.alipay.sofa.jraft.entity.Task;
import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;
import com.hanlin.distribute.client.command.BaseRequestCommand;
import com.hanlin.distribute.client.command.CommandType;
import com.hanlin.distribute.client.command.ResponseCommand;
import com.hanlin.distribute.server.LockServer;
import com.hanlin.distribute.server.closure.LeaderTaskClosure;
import com.hanlin.distribute.server.util.CommandCodec;

import java.nio.ByteBuffer;

/**
 * @author shaoyu
 */
public abstract class BaseRequestProcessor implements RpcProcessor<BaseRequestCommand> {
    
    private LockServer lockServer;
    
    public BaseRequestProcessor(LockServer lockServer) {
        super();
        this.lockServer = lockServer;
    }
    
    @Override
    public void handleRequest(RpcContext rpcCtx, BaseRequestCommand request) {
        if (!lockServer.getFsm().isLeader()) {
            rpcCtx.sendResponse(lockServer.redirect());
            return;
        }
        CommandType cmdType = getCommandType();
        Task task = createTask(rpcCtx, request, cmdType);
        lockServer.getNode().apply(task);
    }
    
    /**
     * 返回类型
     *
     * @return
     */
    protected abstract CommandType getCommandType();
    
    private Task createTask(RpcContext asyncCtx, BaseRequestCommand request, CommandType cmdType) {
        final LeaderTaskClosure closure = new LeaderTaskClosure();
        closure.setCommand(request);
        closure.setCommandType(cmdType);
        closure.setDone(status -> {
            if (status.isOk()) {
                asyncCtx.sendResponse(closure.getResponse());
            } else {
                asyncCtx.sendResponse(new ResponseCommand(false, status.getErrorMsg()));
            }
        });
        // 数据格式：data + type.
        final byte[] cmdBytes = CommandCodec.encodeCommand(request);
        final ByteBuffer data = ByteBuffer.allocate(cmdBytes.length + 1);
        data.put(cmdType.toByte());
        data.put(cmdBytes);
        data.flip();
        return new Task(data, closure);
    }
}
