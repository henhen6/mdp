package top.mddata.base.idempotent.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import top.mddata.base.exception.BizException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 构造时一次性读取并缓存 JSON 请求体的请求包装器，使下游与幂等拦截器可以重复读取请求体。
 *
 * @author henhen6
 */
@Slf4j
public class CachedBodyRequestWrapper extends HttpServletRequestWrapper {

    private static final int BUFFER_SIZE = 8192;

    private final byte[] cachedBody;

    /**
     * 读取并缓存请求体，累计字节数超过上限时立即拒绝。
     *
     * @param request  原始请求
     * @param maxBytes 允许缓存的最大字节数
     */
    public CachedBodyRequestWrapper(HttpServletRequest request, int maxBytes) {
        super(request);
        this.cachedBody = readBody(request, maxBytes);
    }

    /**
     * 返回缓存请求体的副本，避免下游修改内部缓存。
     *
     * @return 原始请求体字节
     */
    public byte[] getCachedBody() {
        return cachedBody.clone();
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedServletInputStream(cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        String encoding = getRequest().getCharacterEncoding();
        Charset charset = (encoding == null || encoding.isBlank())
                ? StandardCharsets.UTF_8
                : Charset.forName(encoding);
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedBody), charset));
    }

    private static byte[] readBody(HttpServletRequest request, int maxBytes) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int total = 0;
            int read;
            try (ServletInputStream input = request.getInputStream()) {
                while ((read = input.read(buffer)) != -1) {
                    total += read;
                    if (total > maxBytes) {
                        throw new BizException(-6, "幂等请求体超过大小限制");
                    }
                    output.write(buffer, 0, read);
                }
            }
            return output.toByteArray();
        } catch (IOException e) {
            log.error("幂等请求体读取失败", e);
            throw new BizException(-1, "幂等请求体读取失败", e);
        }
    }

    /**
     * 基于缓存字节数组的输入流，每次调用 {@link #getInputStream()} 都获得独立游标。
     */
    private static final class CachedServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream source;

        private CachedServletInputStream(byte[] body) {
            this.source = new ByteArrayInputStream(body);
        }

        @Override
        public boolean isFinished() {
            return source.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("缓存请求体不支持异步读取");
        }

        @Override
        public int read() {
            return source.read();
        }

        @Override
        public int read(byte[] buffer, int offset, int length) {
            return source.read(buffer, offset, length);
        }
    }
}
