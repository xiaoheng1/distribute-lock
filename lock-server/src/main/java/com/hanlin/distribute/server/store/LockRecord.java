package com.hanlin.distribute.server.store;

import java.io.Serializable;

/**
 * @author shaoyu
 * 记录锁请求记录, 该记录会被持久化到磁盘.
 */
public class LockRecord implements Serializable {
    
    /**
     * 锁记录 key
     */
    private String lockKey;
    
    /**
     * 锁时间，毫秒数.
     */
    private long lockTime;
    
    public LockRecord() {
    }
    
    public LockRecord(String lockKey, long lockTime) {
        this.lockKey = lockKey;
        this.lockTime = lockTime;
    }
    
    public String getLockKey() {
        return lockKey;
    }
    
    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }
    
    public Long getLockTime() {
        return lockTime;
    }
    
    public void setLockTime(Long lockTime) {
        this.lockTime = lockTime;
    }
}
