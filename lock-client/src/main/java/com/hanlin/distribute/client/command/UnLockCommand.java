package com.hanlin.distribute.client.command;

import java.io.Serializable;

/**
 * @author shaoyu
 */
public class UnLockCommand extends BaseRequestCommand implements Serializable {
    
    public UnLockCommand() {
    
    }
    
    public UnLockCommand(String lockKey) {
        super(lockKey);
    }
}
