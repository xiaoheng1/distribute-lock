package com.hanlin.distribute.client.conf;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author shaoyu
 */
public class ClientStartupConfig {
    
    private String groupId;
    
    private String conf;
    
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
        
        this.groupId = properties.getProperty("groupId");
        this.conf = properties.getProperty("conf");
        
        return validate();
    }
    
    private boolean validate() {
        Assert.notEmpty(groupId, "Raft GroupId 不能为空");
        Assert.notEmpty(conf, "Raft 集群配置不能为空");
        return true;
    }
}
