package com.hanlin.distribute.server.processor;

import com.hanlin.distribute.client.command.CommandType;
import com.hanlin.distribute.client.command.LockCommand;
import com.hanlin.distribute.server.LockServer;

/**
 * @author shaoyu
 */
public class LockRequestProcessor extends BaseRequestProcessor {
    
    public LockRequestProcessor(LockServer lockServer) {
        super(lockServer);
    }
    
    @Override
    protected CommandType getCommandType() {
        return CommandType.LOCK;
    }
    
    @Override
    public String interest() {
        return LockCommand.class.getName();
    }
}
