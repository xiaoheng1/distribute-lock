package com.hanlin.distribute.server.closure;

import cn.hutool.core.util.ObjectUtil;
import com.alipay.sofa.jraft.Closure;
import com.alipay.sofa.jraft.Status;
import com.hanlin.distribute.client.command.BaseRequestCommand;
import com.hanlin.distribute.client.command.CommandType;
import com.hanlin.distribute.client.command.ResponseCommand;

/**
 * @author shaoyu
 */
public class LeaderTaskClosure implements Closure {
    
    /**
     * 请求
     */
    private BaseRequestCommand command;
    
    /**
     * 命令类型
     */
    private CommandType commandType;
    
    /**
     * 回调
     */
    private Closure done;
    
    /**
     * 响应
     */
    private ResponseCommand response;
    
    public BaseRequestCommand getCommand() {
        return command;
    }
    
    public void setCommand(BaseRequestCommand command) {
        this.command = command;
    }
    
    public CommandType getCommandType() {
        return commandType;
    }
    
    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }
    
    public Closure getDone() {
        return done;
    }
    
    public void setDone(Closure done) {
        this.done = done;
    }
    
    public ResponseCommand getResponse() {
        return response;
    }
    
    public void setResponse(ResponseCommand response) {
        this.response = response;
    }
    
    @Override
    public void run(Status status) {
        if (ObjectUtil.isNotEmpty(done)) {
            done.run(status);
        }
    }
}
