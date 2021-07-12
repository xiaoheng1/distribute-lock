package com.hanlin.distribute.server.processor;

import com.hanlin.distribute.client.command.CommandType;
import com.hanlin.distribute.client.command.UnLockCommand;
import com.hanlin.distribute.server.LockServer;

/**
 * @author shaoyu
 */
public class UnLockRequestProcessor extends BaseRequestProcessor{
    
    public UnLockRequestProcessor(LockServer lockServer) {
        super(lockServer);
    }
    
    @Override
    protected CommandType getCommandType() {
        return CommandType.UNLOCK;
    }
    
    @Override
    public String interest() {
        return UnLockCommand.class.getName();
    }
}
