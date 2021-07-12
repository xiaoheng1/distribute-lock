package com.hanlin.distribute.server.util;

import com.alipay.remoting.util.ConcurrentHashSet;

import java.util.concurrent.locks.LockSupport;

/**
 * @author shaoyu
 * 行级锁
 * TODO: 后续会优化加锁问题. 当超过自选次数，则直接挂起.
 */
public class RecordLock {
    
    private final ConcurrentHashSet<String> LOCK_SET = new ConcurrentHashSet<>();
    
    /**
     * 自旋次数.
     */
    private static final int SPIN = 64;
    
    public boolean lock(String key) {
        while (!LOCK_SET.add(key)) {
            LockSupport.parkNanos(1L);
        }
        return true;
    }
    
    public boolean unlock(String key) {
        return LOCK_SET.remove(key);
    }
}
