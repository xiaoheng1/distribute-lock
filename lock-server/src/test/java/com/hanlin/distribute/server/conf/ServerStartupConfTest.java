package com.hanlin.distribute.server.conf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author shaoyu
 */
public class ServerStartupConfTest {
    
    @Test
    public void testLoadFromFile() {
        ServerStartupConfig serverStartupConfig = new ServerStartupConfig();
        boolean b = serverStartupConfig.loadFromFile("127.0.0.1:8081.properties");
        Assertions.assertTrue(b);
        Assertions.assertEquals("127.0.0.1:8081", serverStartupConfig.getServerAddress());
        Assertions.assertEquals("LOCK_DISTRIBUTE", serverStartupConfig.getGroupId());
        Assertions.assertEquals("127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083", serverStartupConfig.getConf());
        Assertions.assertEquals("/Users/xiaoheng/Documents/lock-distribute/tmp/server1", serverStartupConfig.getDataPath());
    }
}
