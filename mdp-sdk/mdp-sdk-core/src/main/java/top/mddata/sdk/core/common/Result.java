package top.mddata.sdk.core.common;

import com.alibaba.fastjson2.annotation.JSONField;
import top.mddata.sdk.core.sign.StringUtils;

/**
 * @author 六如
 */
public class Result<T> {
    private String code;
    private String msg;
    private String subCode;
    private String subMsg;
    private String solution;
    private T data;

    @JSONField(serialize = false)
    public boolean isSuccess() {
        return StringUtils.isEmpty(subCode);
    }

    public String getCode() {
        return code;
    }

    public Result<T> setCode(String code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Result<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getSubCode() {
        return subCode;
    }

    public Result<T> setSubCode(String subCode) {
        this.subCode = subCode;
        return this;
    }

    public String getSubMsg() {
        return subMsg;
    }

    public Result<T> setSubMsg(String subMsg) {
        this.subMsg = subMsg;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

    public String getSolution() {
        return solution;
    }

    public Result<T> setSolution(String solution) {
        this.solution = solution;
        return this;
    }
}
