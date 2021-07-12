package com.hanlin.distribute.server;


import cn.hutool.core.util.ObjectUtil;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.option.ReadOnlyOption;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.RpcServer;
import com.hanlin.distribute.client.command.ResponseCommand;
import com.hanlin.distribute.server.conf.ServerStartupConfig;
import com.hanlin.distribute.server.processor.LockRequestProcessor;
import com.hanlin.distribute.server.processor.UnLockRequestProcessor;
import com.hanlin.distribute.server.store.DistributeLockStateMachine;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author shaoyu 加锁服务.
 */
public class LockServer {
    
    /**
     * 状态机
     */
    private DistributeLockStateMachine fsm;
    
    private RaftGroupService raftGroupService;
    
    /**
     * 节点
     */
    private Node node;
    
    public LockServer(ServerStartupConfig serverStartupConfig) {
        init(serverStartupConfig);
    }
    
    
    private void init(ServerStartupConfig serverStartupConfig) {
        // 初始化路径
        try {
            FileUtils.forceMkdir(new File(serverStartupConfig.getDataPath()));
        } catch (IOException e) {
            throw new IllegalArgumentException("初始化 Raft 数据存储路径失败");
        }
        
        // 初始化状态机
        this.fsm = new DistributeLockStateMachine();
        
        // 创建 NodeOptions
        NodeOptions nodeOptions = new NodeOptions();
        nodeOptions.setElectionTimeoutMs(1000);
        nodeOptions.setDisableCli(false);
        // 解析参数
        final PeerId serverId = new PeerId();
        if (!serverId.parse(serverStartupConfig.getServerAddress())) {
            throw new IllegalArgumentException("初始化节点地址失败");
        }
        Configuration initConf = new Configuration();
        if (initConf.parse(serverStartupConfig.getConf())) {
            throw new IllegalArgumentException("初始化 Raft 集群地址信息失败");
        }
        nodeOptions.setInitialConf(initConf);
        nodeOptions.setFsm(fsm);
        nodeOptions.setEnableMetrics(true);
        nodeOptions.getRaftOptions().setReplicatorPipeline(true);
        nodeOptions.getRaftOptions().setSync(true);
        nodeOptions.getRaftOptions().setReadOnlyOptions(ReadOnlyOption.ReadOnlySafe);
        
        nodeOptions.setSnapshotUri(serverStartupConfig.getDataPath() + File.separator + "snapshot");
        nodeOptions.setLogUri(serverStartupConfig.getDataPath() + File.separator + "log");
        nodeOptions.setRaftMetaUri(serverStartupConfig.getDataPath() + File.separator + "raft_meta");
        
        // 创建 RpcServer
        RpcServer rpcServer = RaftRpcServerFactory.createRaftRpcServer(serverId.getEndpoint());
        // 注册业务处理器
        rpcServer.registerProcessor(new LockRequestProcessor(this));
        rpcServer.registerProcessor(new UnLockRequestProcessor(this));
        
        this.raftGroupService = new RaftGroupService(serverStartupConfig.getGroupId(), serverId, nodeOptions,
                rpcServer);
    }
    
    
    private void start() throws IOException {
        // 启动
        this.node = this.raftGroupService.start();
    }
    
    public DistributeLockStateMachine getFsm() {
        return this.fsm;
    }
    
    public Node getNode () {
        return this.node;
    }
    
    /**
     * 重定向到 leader 节点.
     * @return
     */
    public ResponseCommand redirect() {
        ResponseCommand responseCommand = new ResponseCommand();
        responseCommand.setSuccess(false);
        responseCommand.setErrorMsg("No Leader In Raft Group");
        if (ObjectUtil.isNotEmpty(this.node)) {
            PeerId leaderId = this.node.getLeaderId();
            if(ObjectUtil.isNotEmpty(leaderId)) {
                responseCommand.setRedirect(leaderId.toString());
            }
        }
        return responseCommand;
    }
    
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("请传入配置文件名");
        }
        
        ServerStartupConfig serverStartupConfig = new ServerStartupConfig();
        serverStartupConfig.loadFromFile(args[0]);
        
        // 启动
        final LockServer counterServer = new LockServer(serverStartupConfig);
        counterServer.start();
    }
}
