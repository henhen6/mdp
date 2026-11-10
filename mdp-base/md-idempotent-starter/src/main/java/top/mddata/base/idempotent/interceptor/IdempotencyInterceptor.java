package top.mddata.base.idempotent.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;
import top.mddata.base.annotation.idempotent.Idempotent;
import top.mddata.base.exception.BizException;
import top.mddata.base.idempotent.filter.CachedBodyRequestWrapper;
import top.mddata.base.idempotent.repository.IdempotencyRepository;
import top.mddata.base.idempotent.repository.IdempotencyRepository.Acquisition;
import top.mddata.base.idempotent.repository.IdempotencyRepository.Execution;
import top.mddata.base.idempotent.service.IdempotencyTokenService;
import top.mddata.base.idempotent.support.IdempotencyConstants;
import top.mddata.base.idempotent.support.IdempotencyResponseWriter;
import top.mddata.base.idempotent.support.RequestFingerprint;

import java.io.IOException;

/**
 * 幂等 MVC 拦截器：对标注 {@link Idempotent} 的接口按令牌状态分发——
 * 抢占成功放行进入 Controller，处理中/冲突/令牌失效写统一失败响应，成功结果直接重放首次响应。
 * <p>
 * 释放与结果写入由业务侧通过 {@link Execution}（请求属性）完成；
 * Controller 抛出未知异常时保留 PROCESSING 状态，由运维处理，禁止自动释放。
 *
 * @author henhen6
 */
public class IdempotencyInterceptor implements HandlerInterceptor {

    /** 相同请求正在处理中的业务错误码。 */
    private static final int PROCESSING_CODE = -14;

    /** 请求内容指纹与首次请求冲突的业务错误码。 */
    private static final int CONFLICT_CODE = -15;

    /** 幂等令牌缺失、失效或身份不匹配的业务错误码。 */
    private static final int INVALID_CODE = -16;

    /** 请求未经受控 JSON 包装的业务错误码。 */
    private static final int BODY_UNSUPPORTED_CODE = -6;

    private final IdempotencyRepository repository;
    private final IdempotencyResponseWriter responseWriter;

    /**
     * @param repository     幂等令牌状态仓储
     * @param responseWriter 统一响应写出器
     */
    public IdempotencyInterceptor(IdempotencyRepository repository,
                                  IdempotencyResponseWriter responseWriter) {
        this.repository = repository;
        this.responseWriter = responseWriter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        Idempotent idempotent = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), Idempotent.class);
        if (idempotent == null) {
            return true;
        }
        String token = request.getHeader(IdempotencyConstants.HEADER);
        if (token == null || token.isBlank()) {
            responseWriter.writeError(response, INVALID_CODE, "幂等令牌缺失");
            return false;
        }
        String subject;
        try {
            subject = IdempotencyTokenService.currentSubject();
        } catch (BizException e) {
            responseWriter.writeError(response, INVALID_CODE, "幂等令牌与当前身份不匹配");
            return false;
        }
        // 沿包装链查找缓存体包装：请求可能在后续 Filter（如 XSS 清洗）中被再次包装，
        // CachedBodyRequestWrapper 不一定是最外层，直接 instanceof 会误判
        CachedBodyRequestWrapper wrappedRequest =
                WebUtils.getNativeRequest(request, CachedBodyRequestWrapper.class);
        if (wrappedRequest == null) {
            responseWriter.writeError(response, BODY_UNSUPPORTED_CODE, "幂等请求仅支持受控 JSON 请求");
            return false;
        }

        String fingerprint = RequestFingerprint.sha256(request, wrappedRequest.getCachedBody());
        Acquisition acquisition = repository.acquire(token, subject, fingerprint);
        return switch (acquisition.outcome()) {
            case ACQUIRED -> {
                request.setAttribute(IdempotencyConstants.EXECUTION_ATTRIBUTE,
                        new Execution(token, acquisition.executionId()));
                yield true;
            }
            case PROCESSING -> {
                responseWriter.writeError(response, PROCESSING_CODE, "相同请求正在处理中，请勿重复提交");
                yield false;
            }
            case CONFLICT -> {
                responseWriter.writeError(response, CONFLICT_CODE, "请求内容与首次请求不一致");
                yield false;
            }
            case INVALID -> {
                responseWriter.writeError(response, INVALID_CODE, "幂等令牌无效或已过期");
                yield false;
            }
            case REPLAY -> {
                responseWriter.replay(response, acquisition.response());
                yield false;
            }
        };
    }
}
