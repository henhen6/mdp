package top.mddata.open.service.notify.pipeline;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.gitee.sop.support.aes.AesException;
import com.gitee.sop.support.aes.MdpBizMsgCrypt;
import com.gitee.sop.support.message.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.open.enumeration.admin.NotifyEncryptionTypeEnum;
import top.mddata.open.service.notify.NotifyProperties;

import java.time.LocalDateTime;

/**
 * 统一投递器：加签/加密 → HTTP POST → 结果判定。
 * 所有失败（加密失败、超时、连接失败、业务 code 非 0、响应非 JSON）归一为 NotifyResult.fail，不向外抛异常，
 * 由调用方（NotifyTaskExecutor）统一做状态流转，避免两份复制粘贴的 doNotify。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifySender {

    private final NotifyProperties notifyProperties;

    public NotifyResult send(NotifyTaskView task) {
        LocalDateTime requestTime = LocalDateTime.now();
        String requestUrl;
        String requestBody;
        try {
            int encryptionMode = task.getNotifyEncryptionType() != null
                    ? task.getNotifyEncryptionType() : NotifyEncryptionTypeEnum.PLAINTEXT.getCode();
            MdpBizMsgCrypt crypt = new MdpBizMsgCrypt(
                    task.getNotifyToken(), task.getNotifyEncodingAesKey(), task.getAppKey());
            String timestamp = MdpBizMsgCrypt.generateTimestamp();
            String nonce = MdpBizMsgCrypt.generateNonce();
            JSONObject body = crypt.encryptMsg(task.buildPlaintext(), timestamp, nonce, encryptionMode);
            requestBody = body.toJSONString();
            requestUrl = MdpBizMsgCrypt.appendUrlParams(task.getNotifyUrl(),
                    crypt.getSignature(), crypt.getMsgSignature(), timestamp, nonce,
                    encryptionMode == NotifyEncryptionTypeEnum.PLAINTEXT.getCode() ? null : "aes");
        } catch (AesException e) {
            return NotifyResult.fail("消息加密失败：" + e.getMessage(),
                    task.getNotifyUrl(), null, null, requestTime, LocalDateTime.now());
        }

        log.info("[推送] 发送请求, taskId={}, url={}", task.getId(), requestUrl);
        try (HttpResponse response = HttpRequest.post(requestUrl)
                .timeout(notifyProperties.getTimeout())
                .body(requestBody)
                .execute()) {
            String responseBody = response.body();
            return judge(requestUrl, requestBody, responseBody, requestTime);
        } catch (Exception e) {
            log.warn("[推送] 请求异常, taskId={}, url={}", task.getId(), requestUrl, e);
            return NotifyResult.fail("请求异常：" + e.getMessage(),
                    requestUrl, requestBody, null, requestTime, LocalDateTime.now());
        }
    }

    /** 成功判定：HTTP 200 且响应体能解析出 code=成功码 */
    private NotifyResult judge(String requestUrl, String requestBody, String responseBody, LocalDateTime requestTime) {
        LocalDateTime responseTime = LocalDateTime.now();
        try {
            ApiResponse r = JSON.parseObject(responseBody, ApiResponse.class);
            if (r != null && ApiResponse.SUCCESS_CODE.equals(r.getCode())) {
                return NotifyResult.ok(requestUrl, requestBody, responseBody, requestTime, responseTime);
            }
            if (r == null) {
                // 空响应体或无法映射为响应对象：对方未按协议返回，归为格式错误
                return NotifyResult.fail("返回值格式错误，无法解析为 ApiResponse.class，请返回正确格式：{\"code\": 0, \"msg\": \"\"}",
                        requestUrl, requestBody, responseBody, requestTime, responseTime);
            }
            return NotifyResult.fail("对方返回失败：" + responseBody,
                    requestUrl, requestBody, responseBody, requestTime, responseTime);
        } catch (JSONException e) {
            return NotifyResult.fail("返回值格式错误，无法解析为 ApiResponse.class，请返回正确格式：{\"code\": 0, \"msg\": \"\"}",
                    requestUrl, requestBody, responseBody, requestTime, responseTime);
        }
    }
}
