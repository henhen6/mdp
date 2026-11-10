package top.mddata.base.idempotent.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import top.mddata.base.exception.BizException;
import top.mddata.base.idempotent.properties.IdempotencyProperties;
import top.mddata.base.idempotent.support.IdempotencyConstants;
import top.mddata.base.idempotent.support.IdempotencyResponseWriter;

import java.io.IOException;

/**
 * 幂等请求过滤器：仅对携带幂等请求头的 JSON 请求缓存请求体，其余请求零包装原样放行。
 * <p>
 * 缓存阶段的业务异常在过滤器内转成统一失败响应，不进入业务 Controller；
 * Controller 自身抛出的业务异常不在此捕获，保持全局异常处理不变。
 *
 * @author henhen6
 */
@Slf4j
public class IdempotencyRequestFilter implements Filter {

    private final IdempotencyProperties properties;
    private final IdempotencyResponseWriter responseWriter;

    /**
     * @param properties     幂等配置
     * @param responseWriter 统一响应写出器
     */
    public IdempotencyRequestFilter(IdempotencyProperties properties,
                                    IdempotencyResponseWriter responseWriter) {
        this.properties = properties;
        this.responseWriter = responseWriter;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!needsCachedBody(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        int maxBytes = Math.toIntExact(properties.getMaxRequestBody().toBytes());
        CachedBodyRequestWrapper wrapper;
        try {
            wrapper = new CachedBodyRequestWrapper(httpRequest, maxBytes);
        } catch (BizException e) {
            log.warn("幂等请求被拒绝，code={}，message={}", e.getCode(), e.getMessage());
            responseWriter.writeError(httpResponse, e.getCode(), e.getMessage());
            return;
        }
        chain.doFilter(wrapper, response);
    }

    private static boolean needsCachedBody(HttpServletRequest request) {
        String idempotencyKey = request.getHeader(IdempotencyConstants.HEADER);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return false;
        }
        return isJsonCompatible(request.getContentType());
    }

    private static boolean isJsonCompatible(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return false;
        }
        try {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            String subtype = mediaType.getSubtype().toLowerCase();
            return "json".equals(subtype) || subtype.endsWith("+json");
        } catch (InvalidMediaTypeException e) {
            return false;
        }
    }
}
