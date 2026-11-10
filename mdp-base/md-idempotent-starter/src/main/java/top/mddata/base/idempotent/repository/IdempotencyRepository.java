package top.mddata.base.idempotent.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 幂等令牌状态仓储，负责令牌签发、请求抢占、结果写入与释放。
 *
 * @author henhen6
 */
public interface IdempotencyRepository {

    /**
     * 签发未使用的幂等令牌。
     *
     * @param token   幂等令牌
     * @param subject 令牌归属身份
     */
    void issue(String token, String subject);

    /**
     * 抢占幂等令牌。
     *
     * @param token       幂等令牌
     * @param subject     请求身份
     * @param fingerprint 请求内容指纹
     * @return 抢占结果
     */
    Acquisition acquire(String token, String subject, String fingerprint);

    /**
     * 写入首个完成响应并将令牌置为成功状态。
     *
     * @param token       幂等令牌
     * @param executionId 抢占时获得的执行身份
     * @param response    缓存响应
     */
    void complete(String token, String executionId, CachedResponse response);

    /**
     * 释放处理中令牌，使其恢复为未使用状态。
     *
     * @param token       幂等令牌
     * @param executionId 抢占时获得的执行身份
     * @return 是否释放成功
     */
    boolean release(String token, String executionId);

    /** 抢占结果类型。 */
    enum Outcome {
        /** 抢占成功，获得执行权。 */
        ACQUIRED,
        /** 相同请求正在处理中。 */
        PROCESSING,
        /** 令牌已有成功响应，可直接重放。 */
        REPLAY,
        /** 请求内容指纹与原请求冲突。 */
        CONFLICT,
        /** 令牌不存在或身份不匹配。 */
        INVALID
    }

    /**
     * 令牌与执行身份的绑定关系。
     *
     * @param token       幂等令牌
     * @param executionId 执行身份
     */
    record Execution(String token, String executionId) {
    }

    /**
     * 可重放的缓存响应。
     * <p>
     * 字段名与 Lua 脚本固定契约一致，显式声明以抵消注入 ObjectMapper 的命名策略。
     *
     * @param status      HTTP 状态码
     * @param contentType 响应内容类型
     * @param body        响应体
     */
    record CachedResponse(@JsonProperty("status") int status,
                          @JsonProperty("contentType") String contentType,
                          @JsonProperty("body") String body) {
    }

    /**
     * 抢占结果。
     * <p>
     * 字段名与 Lua 脚本固定契约一致，显式声明以抵消注入 ObjectMapper 的命名策略。
     *
     * @param outcome     结果类型
     * @param executionId 抢占成功时的执行身份，其他结果可能为 null
     * @param response    重放结果对应的缓存响应，其他结果为 null
     */
    record Acquisition(@JsonProperty("outcome") Outcome outcome,
                       @JsonProperty("executionId") String executionId,
                       @JsonProperty("response") CachedResponse response) {
    }
}
