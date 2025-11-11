package com.gitee.sop.support.service.dto;

import lombok.Data;

import java.io.Serializable;


/**
 * @author 六如
 */
@Data
public class RegisterResult implements Serializable {
    private static final long serialVersionUID = 4605838522659698419L;

    private Boolean success;

    private String msg;

    public static RegisterResult success() {
        RegisterResult registerResult = new RegisterResult();
        registerResult.setSuccess(true);
        return registerResult;
    }

    public static RegisterResult error(String msg) {
        RegisterResult registerResult = new RegisterResult();
        registerResult.setSuccess(false);
        registerResult.setMsg(msg);
        return registerResult;
    }

}
