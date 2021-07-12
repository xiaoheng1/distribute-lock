package com.hanlin.distribute.client.command;

import java.io.Serializable;

/**
 * @author shaoyu
 */
public class ResponseCommand implements Serializable {
    
    /**
     * 成功或失败
     */
    private Boolean success;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    /**
     * 重定向到哪一个节点.
     */
    private String redirect;
    
    public ResponseCommand() {
        super();
    }
    
    public ResponseCommand(Boolean success) {
        super();
        this.success = success;
    }
    
    public ResponseCommand(Boolean success, String errorMsg) {
        super();
        this.success = success;
        this.errorMsg = errorMsg;
    }
    
    public Boolean getSuccess() {
        return success;
    }
    
    public void setSuccess(Boolean success) {
        this.success = success;
    }
    
    public String getErrorMsg() {
        return errorMsg;
    }
    
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    
    public String getRedirect() {
        return redirect;
    }
    
    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }
}
