package com.hanlin.distribute.server.conf;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author shaoyu
 */
public class ServerStartupConfig {
    
    /**
     * 数据存储路径
     */
    private String dataPath;
    
    /**
     * raft group id
     */
    private String groupId;
    
    /**
     * 配置：address1, address2, address3
     */
    private String conf;
    
    /**
     * 服务端地址
     */
    private String serverAddress;
    
    public boolean loadFromFile(String confFileName) {
        InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream(confFileName);
        if (ObjectUtil.isNull(systemResourceAsStream)) {
            throw new IllegalArgumentException("配置文件不存在");
        }
        
        Properties properties = new Properties();
        try {
            properties.load(systemResourceAsStream);
        } catch (IOException e) {
            throw new IllegalStateException("读取配置文件失败", e);
        }
        
        this.dataPath = properties.getProperty("dataPath");
        this.groupId = properties.getProperty("groupId");
        this.conf = properties.getProperty("conf");
        this.serverAddress = properties.getProperty("serverAddress");
        
        return validate();
    }
    
    private boolean validate() {
        Assert.notEmpty(dataPath, "数据存储理解不能为空");
        Assert.notEmpty(groupId, "Raft GroupId 不能为空");
        Assert.notEmpty(conf, "Raft 集群配置不能为空");
        Assert.notEmpty(serverAddress, "节点地址不能为空");
        return true;
    }
    
    public String getDataPath() {
        return dataPath;
    }
    
    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getConf() {
        return conf;
    }
    
    public void setConf(String conf) {
        this.conf = conf;
    }
    
    public String getServerAddress() {
        return serverAddress;
    }
    
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
}
