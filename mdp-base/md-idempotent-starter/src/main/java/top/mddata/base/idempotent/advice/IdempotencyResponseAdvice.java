package top.mddata.base.idempotent.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import top.mddata.base.base.R;
import top.mddata.base.constant.Constants;
import top.mddata.base.idempotent.properties.IdempotencyProperties;
import top.mddata.base.idempotent.repository.IdempotencyRepository;
import top.mddata.base.idempotent.repository.IdempotencyRepository.CachedResponse;
import top.mddata.base.idempotent.repository.IdempotencyRepository.Execution;
import top.mddata.base.idempotent.support.IdempotencyConstants;

import java.io.UncheckedIOException;

/**
 * 幂等接口的最终响应缓存：成功时缓存首个响应供重放，明确业务失败时释放令牌，
 * 其他不确定结果保持 PROCESSING，绝不为了方便重试而释放。
 * <p>
 * 真实应用组件扫描会装配本类，须与自动配置同条件退避，保证 {@code mdp.idempotent.enabled=false} 时整体关闭。
 *
 * @author henhen6
 */
@Slf4j
@ControllerAdvice
@ConditionalOnProperty(prefix = IdempotencyProperties.PREFIX,
        name = Constants.ENABLED, havingValue = "true", matchIfMissing = true)
@Order(Ordered.LOWEST_PRECEDENCE)
public class IdempotencyResponseAdvice implements ResponseBodyAdvice<Object> {

    /** 释放失败时追加到业务消息的提示后缀。 */
    private static final String RELEASE_FAILED_SUFFIX = "；幂等状态释放失败";

    private final IdempotencyRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * @param repository   幂令牌状态仓储
     * @param objectMapper 与应用一致的 JSON 序列化器
     */
    public IdempotencyResponseAdvice(IdempotencyRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)
                || !(response instanceof ServletServerHttpResponse servletResponse)) {
            return body;
        }
        Object attribute =
                servletRequest.getServletRequest().getAttribute(IdempotencyConstants.EXECUTION_ATTRIBUTE);
        if (!(attribute instanceof Execution execution)) {
            return body;
        }
        // 首次通知即终结本次执行，避免响应处理异常后的全局错误响应再次消费并错误释放令牌
        servletRequest.getServletRequest().removeAttribute(IdempotencyConstants.EXECUTION_ATTRIBUTE);
        if (!(body instanceof R<?> r)) {
            // 非 R 响应属于不确定结果：令牌保持 PROCESSING，禁止释放
            throw new IllegalStateException("幂等接口必须返回 R 响应");
        }
        if (r.getCode() == R.SUCCESS_CODE) {
            return complete(execution, r, selectedContentType, servletResponse);
        }
        return release(execution, r);
    }

    /**
     * 缓存首个成功响应并落库，序列化或仓储异常向上抛，令牌保持 PROCESSING。
     */
    private Object complete(Execution execution, R<?> body, MediaType selectedContentType,
                            ServletServerHttpResponse response) {
        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException("幂等响应序列化失败", e);
        }
        CachedResponse cached = new CachedResponse(
                response.getServletResponse().getStatus(),
                selectedContentType.toString(),
                json);
        repository.complete(execution.token(), execution.executionId(), cached);
        return body;
    }

    /**
     * 明确业务失败时释放令牌；释放异常只追加提示并照常返回业务响应，日志不携带令牌或响应体。
     */
    private Object release(Execution execution, R<?> body) {
        try {
            repository.release(execution.token(), execution.executionId());
            return body;
        } catch (RuntimeException e) {
            log.error("幂等令牌释放阶段失败，业务失败响应照常返回，异常类型: {}", e.getClass().getName());
            String original = body.getMsg();
            body.setMsg(original == null || original.isBlank()
                    ? RELEASE_FAILED_SUFFIX.strip() : original + RELEASE_FAILED_SUFFIX);
            return body;
        }
    }
}
