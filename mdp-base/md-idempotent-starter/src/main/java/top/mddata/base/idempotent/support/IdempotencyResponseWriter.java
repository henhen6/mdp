package top.mddata.base.idempotent.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import top.mddata.base.base.R;
import top.mddata.base.idempotent.repository.IdempotencyRepository.CachedResponse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 幂等组件的响应写出器：向客户端输出统一格式错误，或按缓存内容重放首个成功响应。
 *
 * @author henhen6
 */
@Slf4j
public class IdempotencyResponseWriter {

    private static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

    private final ObjectMapper objectMapper;

    /**
     * @param objectMapper 与应用一致的 JSON 序列化器
     */
    public IdempotencyResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 输出 HTTP 200 与统一失败包装，请求在此终止，不进入业务 Controller。
     *
     * @param response 响应
     * @param code     业务错误码
     * @param message  错误消息
     * @throws IOException 响应写出失败时向外抛出，尽早暴露
     */
    public void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(JSON_CONTENT_TYPE);
        response.getOutputStream().write(toJsonBytes(R.fail(code, message)));
    }

    /**
     * 按缓存内容重放响应，只恢复 HTTP 状态码、Content-Type 与响应体，不复制任何其他响应头。
     *
     * @param response 响应
     * @param cached   首个请求的缓存响应
     * @throws IOException 响应写出失败时向外抛出，尽早暴露
     */
    public void replay(HttpServletResponse response, CachedResponse cached) throws IOException {
        response.setStatus(cached.status());
        String contentType = cached.contentType();
        if (contentType != null && !contentType.isBlank()) {
            response.setContentType(contentType);
        }
        // body 字节编码必须与 Content-Type 声明的 charset 一致，否则非 UTF-8 响应重放后乱码
        response.getOutputStream().write(cached.body().getBytes(resolveCharset(contentType)));
    }

    private static Charset resolveCharset(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return StandardCharsets.UTF_8;
        }
        try {
            Charset declared = MediaType.parseMediaType(contentType).getCharset();
            return declared != null ? declared : StandardCharsets.UTF_8;
        } catch (InvalidMediaTypeException e) {
            // 无法从非法 Content-Type 取得 charset，回退 UTF-8 与无 charset 场景保持一致
            return StandardCharsets.UTF_8;
        }
    }

    private byte[] toJsonBytes(Object value) throws JsonProcessingException {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            log.error("幂等错误响应序列化失败", e);
            throw e;
        }
    }
}
