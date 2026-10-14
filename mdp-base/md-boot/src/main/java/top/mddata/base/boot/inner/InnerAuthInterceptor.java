package top.mddata.base.boot.inner;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 内部端点令牌校验：/inner/** 仅供平台内部服务间调用，
 * 通过共享令牌头防止端点裸露在 HTTP 面上（仅靠 @Operation(hidden) 隐藏文档等于无保护）。
 *
 * @author henhen
 */
@Slf4j
public class InnerAuthInterceptor implements HandlerInterceptor {

    public static final String INNER_TOKEN_HEADER = "X-Inner-Token";

    private final String innerToken;

    public InnerAuthInterceptor(String innerToken) {
        this.innerToken = innerToken;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (innerToken == null || innerToken.isBlank()) {
            // 未配置令牌时拒绝一切调用：宁可内部调用失败，也不让端点裸奔
            log.error("[内部接口] mdp.inner.token 未配置，拒绝访问 {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "内部接口未启用");
            return false;
        }
        if (!innerToken.equals(request.getHeader(INNER_TOKEN_HEADER))) {
            log.warn("[内部接口] 令牌校验失败, uri={}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "内部接口令牌无效");
            return false;
        }
        return true;
    }
}
