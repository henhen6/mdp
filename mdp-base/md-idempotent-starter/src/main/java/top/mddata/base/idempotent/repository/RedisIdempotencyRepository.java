package top.mddata.base.idempotent.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import top.mddata.base.cache.repository.CachePlusOps;
import top.mddata.base.idempotent.properties.IdempotencyProperties;

import java.util.List;
import java.util.UUID;

/**
 * 基于 Redis Lua 脚本的幂等状态仓储。
 * <p>
 * 所有状态判定与转换都在单段 Lua 脚本内原子完成，Java 侧不做先读后写。
 *
 * @author henhen6
 */
@Slf4j
public class RedisIdempotencyRepository implements IdempotencyRepository {

    /** 签发脚本；ARGV = stateJson, unusedTtlMillis。 */
    private static final String ISSUE_SCRIPT = """
            local inserted = redis.call('SET', KEYS[1], ARGV[1], 'NX', 'PX', ARGV[2])
            if not inserted then
                return 0
            end
            return 1
            """;

    /** 抢占脚本；ARGV = subject, fingerprint, executionId, processingTtlMillis。 */
    private static final String ACQUIRE_SCRIPT = """
            local result = { outcome = 'INVALID' }
            local raw = redis.call('GET', KEYS[1])
            if not raw then
                return cjson.encode(result)
            end
            local record = cjson.decode(raw)
            if record.subject ~= ARGV[1] then
                return cjson.encode(result)
            end
            if record.state == 'UNUSED' then
                record.state = 'PROCESSING'
                record.fingerprint = ARGV[2]
                record.executionId = ARGV[3]
                redis.call('SET', KEYS[1], cjson.encode(record), 'PX', ARGV[4])
                return cjson.encode({ outcome = 'ACQUIRED', executionId = ARGV[3] })
            end
            if record.fingerprint ~= ARGV[2] then
                result.outcome = 'CONFLICT'
                return cjson.encode(result)
            end
            if record.state == 'PROCESSING' then
                result.outcome = 'PROCESSING'
                return cjson.encode(result)
            end
            if record.state == 'SUCCESS' then
                result.outcome = 'REPLAY'
                result.response = {
                    status = record.status,
                    contentType = record.contentType,
                    body = record.responseBody
                }
                return cjson.encode(result)
            end
            return cjson.encode(result)
            """;

    /** 完成脚本；ARGV = executionId, responseBody, contentType, status, successTtlMillis。 */
    private static final String COMPLETE_SCRIPT = """
            local raw = redis.call('GET', KEYS[1])
            if not raw then
                return 0
            end
            local record = cjson.decode(raw)
            if record.state ~= 'PROCESSING' or record.executionId ~= ARGV[1] then
                return 0
            end
            record.state = 'SUCCESS'
            record.responseBody = ARGV[2]
            record.contentType = ARGV[3]
            record.status = tonumber(ARGV[4])
            redis.call('SET', KEYS[1], cjson.encode(record), 'PX', ARGV[5])
            return 1
            """;

    /** 释放脚本；ARGV = executionId, unusedTtlMillis。 */
    private static final String RELEASE_SCRIPT = """
            local raw = redis.call('GET', KEYS[1])
            if not raw then
                return 0
            end
            local record = cjson.decode(raw)
            if record.state ~= 'PROCESSING' or record.executionId ~= ARGV[1] then
                return 0
            end
            record.state = 'UNUSED'
            record.fingerprint = cjson.null
            record.executionId = cjson.null
            record.responseBody = cjson.null
            record.contentType = cjson.null
            record.status = cjson.null
            redis.call('SET', KEYS[1], cjson.encode(record), 'PX', ARGV[2])
            return 1
            """;

    private final CachePlusOps cachePlusOps;
    private final ObjectMapper objectMapper;
    private final IdempotencyProperties properties;

    public RedisIdempotencyRepository(CachePlusOps cachePlusOps, ObjectMapper objectMapper,
                                      IdempotencyProperties properties) {
        this.cachePlusOps = cachePlusOps;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * Redis 中存储的幂等状态。
     * <p>
     * 字段名与 Lua 脚本固定契约一致，显式声明以抵消注入 ObjectMapper 的命名策略；
     * 始终包含 null 字段，保证 Lua 读取到的记录结构稳定。
     *
     * @param state        状态（UNUSED/PROCESSING/SUCCESS）
     * @param subject      令牌归属身份
     * @param fingerprint  请求内容指纹
     * @param executionId  执行身份
     * @param status       缓存响应的 HTTP 状态码
     * @param contentType  缓存响应内容类型
     * @param responseBody 缓存响应体
     */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private record State(@JsonProperty("state") String state,
                         @JsonProperty("subject") String subject,
                         @JsonProperty("fingerprint") String fingerprint,
                         @JsonProperty("executionId") String executionId,
                         @JsonProperty("status") Integer status,
                         @JsonProperty("contentType") String contentType,
                         @JsonProperty("responseBody") String responseBody) {
    }

    @Override
    public void issue(String token, String subject) {
        String stateJson = writeState(new State("UNUSED", subject, null, null, null, null, null));
        Long result = cachePlusOps.eval(ISSUE_SCRIPT, List.of(buildKey(token)),
                List.of(stateJson, String.valueOf(properties.getUnusedTtl().toMillis())),
                Long.class);
        if (result == null || result != 1L) {
            throw new IllegalStateException("幂等令牌签发冲突");
        }
    }

    @Override
    public Acquisition acquire(String token, String subject, String fingerprint) {
        String raw = cachePlusOps.eval(ACQUIRE_SCRIPT, List.of(buildKey(token)), List.of(subject, fingerprint, UUID.randomUUID().toString(),
                        String.valueOf(properties.getProcessingTtl().toMillis())),
                String.class);
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("幂等抢占脚本返回空结果");
        }
        try {
            return objectMapper.readValue(raw, Acquisition.class);
        } catch (JsonProcessingException e) {
            log.error("幂等抢占结果解析失败", e);
            throw new IllegalStateException("幂等抢占结果解析失败", e);
        }
    }

    @Override
    public void complete(String token, String executionId, CachedResponse response) {
        Long result = cachePlusOps.eval(COMPLETE_SCRIPT, List.of(buildKey(token)),
                List.of(executionId, response.body(), response.contentType(),
                        String.valueOf(response.status()),
                        String.valueOf(properties.getSuccessTtl().toMillis())),
                Long.class);
        if (result == null || result != 1L) {
            throw new IllegalStateException("幂等响应写入失败：执行身份不匹配或令牌状态已变更");
        }
    }

    @Override
    public boolean release(String token, String executionId) {
        Long result = cachePlusOps.eval(RELEASE_SCRIPT, List.of(buildKey(token)),
                List.of(executionId, String.valueOf(properties.getUnusedTtl().toMillis())),
                Long.class);
        return result != null && result == 1L;
    }

    private String buildKey(String token) {
        return properties.getKeyPrefix() + token;
    }

    private String writeState(State state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.error("幂等状态序列化失败", e);
            throw new IllegalStateException("幂等状态序列化失败", e);
        }
    }
}
