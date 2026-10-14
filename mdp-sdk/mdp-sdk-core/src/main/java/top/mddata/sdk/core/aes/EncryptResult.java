package top.mddata.sdk.core.aes;

import com.alibaba.fastjson2.JSONObject;

/**
 * 加密结果：报文体 + 签名。
 * 替代旧版将签名写入 MdpBizMsgCrypt 实例字段的做法（共享实例并发加密时签名会串值）。
 *
 * @author henhen
 */
public class EncryptResult {

    private final JSONObject body;
    private final String signature;
    private final String msgSignature;

    public EncryptResult(JSONObject body, String signature, String msgSignature) {
        this.body = body;
        this.signature = signature;
        this.msgSignature = msgSignature;
    }

    public JSONObject getBody() {
        return body;
    }

    public String getSignature() {
        return signature;
    }

    public String getMsgSignature() {
        return msgSignature;
    }
}
