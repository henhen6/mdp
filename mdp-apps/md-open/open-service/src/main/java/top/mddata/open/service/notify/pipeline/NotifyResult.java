package top.mddata.open.service.notify.pipeline;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 一次投递的结果（不可变）。成功与失败的请求/响应报文都保留，供写日志使用。
 */
@Getter
public final class NotifyResult {

    private final boolean success;
    private final String errorMsg;
    private final String requestUrl;
    private final String requestBody;
    private final String responseBody;
    private final LocalDateTime requestTime;
    private final LocalDateTime responseTime;

    private NotifyResult(boolean success, String errorMsg, String requestUrl, String requestBody,
                         String responseBody, LocalDateTime requestTime, LocalDateTime responseTime) {
        this.success = success;
        this.errorMsg = errorMsg;
        this.requestUrl = requestUrl;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.requestTime = requestTime;
        this.responseTime = responseTime;
    }

    public static NotifyResult ok(String requestUrl, String requestBody, String responseBody,
                                  LocalDateTime requestTime, LocalDateTime responseTime) {
        return new NotifyResult(true, "", requestUrl, requestBody, responseBody, requestTime, responseTime);
    }

    public static NotifyResult fail(String errorMsg, String requestUrl, String requestBody, String responseBody,
                                    LocalDateTime requestTime, LocalDateTime responseTime) {
        return new NotifyResult(false, errorMsg, requestUrl, requestBody, responseBody, requestTime, responseTime);
    }
}
