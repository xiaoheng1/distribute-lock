package com.hanlin.distribute.client.service;


/**
 * @author shaoyu
 */
public interface LockClientService {
    
    
    /**
     * 加锁
     * @param lockKey
     * @param lockTime
     * @return
     */
    boolean lock(String lockKey, long lockTime);
    
    /**
     * 解锁
     * @param lockKey
     * @return
     */
    boolean unlock(String lockKey);
}
