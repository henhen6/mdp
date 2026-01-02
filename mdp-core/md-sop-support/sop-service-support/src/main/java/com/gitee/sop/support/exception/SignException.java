package com.gitee.sop.support.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * @author 六如
 */
@Getter
public class SignException extends Exception {

    @Serial
    private static final long serialVersionUID = 4049365045207852768L;


    private String errCode;
    private String subCode;
    private String errMsg;

    public SignException() {
        super();
    }

    public SignException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignException(String message) {
        super(message);
    }

    public SignException(Throwable cause) {
        super(cause);
    }

    public SignException(String errCode, String errMsg) {
        super(errCode + ":" + errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public SignException(String errCode, String subCode, Throwable cause) {
        super(subCode, cause);
        this.errCode = errCode;
        this.subCode = subCode;
    }

}
