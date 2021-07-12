package com.hanlin.distribute.client.command;

/**
 * @author shaoyu
 */
public abstract class BaseRequestCommand {
    
    /**
     * 请求 key
     */
    private String lockKey;
    
    public String getLockKey() {
        return lockKey;
    }
    
    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }
    
    public BaseRequestCommand() {
    }
    
    public BaseRequestCommand(String lockKey) {
        this.lockKey = lockKey;
    }
}
