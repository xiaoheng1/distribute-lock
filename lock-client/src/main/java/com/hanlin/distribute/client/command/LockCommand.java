package com.hanlin.distribute.client.command;

import java.io.Serializable;

/**
 * @author shaoyu 锁请求
 */
public class LockCommand extends BaseRequestCommand implements Serializable {
    
    /**
     * 锁定时间，单位 ms.
     */
    private Long lockTime;
    
    public LockCommand() {
    }
    
    public LockCommand(long lockTime) {
        this.lockTime = lockTime;
    }
    
    public LockCommand(String lockKey, long lockTime) {
        super(lockKey);
        this.lockTime = lockTime;
    }
    
    public Long getLockTime() {
        return lockTime;
    }
    
    public void setLockTime(Long lockTime) {
        this.lockTime = lockTime;
    }
}
