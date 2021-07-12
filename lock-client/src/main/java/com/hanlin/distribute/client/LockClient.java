package com.hanlin.distribute.client;

import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.error.RemotingException;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.RpcClient;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import com.hanlin.distribute.client.command.LockCommand;
import com.hanlin.distribute.client.command.ResponseCommand;
import com.hanlin.distribute.client.command.UnLockCommand;
import com.hanlin.distribute.client.conf.ClientStartupConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

/**
 * @author shaoyu
 */
public class LockClient {
    
    private CliClientServiceImpl cliClientService;
    
    private CliOptions cliOptions;
    
    private Configuration configuration;
    
    private RpcClient rpcClient;
    
    private String groupId;
    
    private static final Logger LOG = LoggerFactory.getLogger(LockClient.class);
    
    public LockClient(ClientStartupConfig clientStartupConfig) {
        init(clientStartupConfig);
    }
    
    public void init(ClientStartupConfig clientStartupConfig) {
        this.configuration = new Configuration();
        if (this.configuration.parse(clientStartupConfig.getConf())) {
            throw new IllegalArgumentException("初始化 Raft 集群地址信息失败");
        }
        
        this.groupId = clientStartupConfig.getGroupId();
        
        this.cliOptions = new CliOptions();
        this.cliClientService = new CliClientServiceImpl();
        cliClientService.init(this.cliOptions);
        
        this.rpcClient = this.cliClientService.getRpcClient();
    }
    
    public boolean start() throws TimeoutException, InterruptedException {
        refreshConfig(this.groupId);
        refreshLeader(this.groupId);
        return true;
    }
    
    private void refreshLeader(String groupId) throws TimeoutException, InterruptedException {
        if (!RouteTable.getInstance().refreshLeader(cliClientService, groupId, this.cliOptions.getTimeoutMs()).isOk()) {
            throw new IllegalStateException("Refresh leader failed");
        }
    }
    
    private void refreshConfig(String groupId) {
        RouteTable.getInstance().updateConfiguration(groupId, this.configuration);
    }
    
    private PeerId getLeader() throws InterruptedException, TimeoutException {
        refreshLeader(this.groupId);
        return RouteTable.getInstance().selectLeader(this.groupId);
    }
    
    public boolean lock(String lockKey, long lockTime)
            throws TimeoutException, InterruptedException, RemotingException {
        LockCommand lockCommand = new LockCommand(lockKey, lockTime);
        Object result = this.rpcClient
                .invokeSync(getLeader().getEndpoint(), lockCommand, this.cliOptions.getRpcDefaultTimeout());
        ResponseCommand response = (ResponseCommand) result;
        if (!response.getSuccess()) {
            LOG.error("server error is " + response.getErrorMsg());
        }
        return response.getSuccess();
    }
    
    public boolean unlock(String lockKey) throws TimeoutException, InterruptedException, RemotingException {
        UnLockCommand unLockCommand = new UnLockCommand(lockKey);
        Object result = this.rpcClient
                .invokeSync(getLeader().getEndpoint(), unLockCommand, this.cliOptions.getRpcDefaultTimeout());
        ResponseCommand response = (ResponseCommand) result;
        if (!response.getSuccess()) {
            LOG.error("server error is " + response.getErrorMsg());
        }
        return response.getSuccess();
    }
    
    public static void main(String[] args) throws TimeoutException, InterruptedException {
        if (args.length != 1) {
            throw new IllegalArgumentException("请传入配置文件名");
        }
        
        ClientStartupConfig serverStartupConfig = new ClientStartupConfig();
        serverStartupConfig.loadFromFile(args[0]);
        
        // 启动
        final LockClient counterServer = new LockClient(serverStartupConfig);
        counterServer.start();
    }
}
