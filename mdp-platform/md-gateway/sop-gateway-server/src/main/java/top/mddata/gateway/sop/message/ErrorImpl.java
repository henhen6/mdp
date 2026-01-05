package top.mddata.gateway.sop.message;

import lombok.Data;

/**
 * @author 六如
 */
@Data
public class ErrorImpl implements IError {

    private String code;
    private String msg;
    private String subCode;
    private String subMsg;
    private String solution;

    public ErrorImpl(String code, String msg, String subCode, String subMsg, String solution) {
        this.code = code;
        this.msg = msg;
        this.subCode = subCode;
        this.subMsg = subMsg;
        this.solution = solution;
    }
}
