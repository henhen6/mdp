package top.mddata.base.idempotent.support;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 请求内容指纹，按 HTTP 方法、服务内部路径、原始 QueryString、原始请求体顺序计算 SHA-256。
 * <p>
 * 任一片段不同都会得到不同指纹，用于识别同一幂等令牌下的重复提交与冲突请求。
 *
 * @author henhen6
 */
public final class RequestFingerprint {

    private static final String ALGORITHM = "SHA-256";
    private static final byte SEPARATOR = '\n';

    private RequestFingerprint() {
    }

    /**
     * 计算请求指纹。
     * <p>
     * 摘要内容依次为：方法、换行、请求 URI、换行、QueryString（null 视为空串）、换行、原始请求体。
     *
     * @param request 请求
     * @param body    已缓存的原始请求体
     * @return 十六进制小写指纹
     */
    public static String sha256(HttpServletRequest request, byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(request.getMethod().getBytes(StandardCharsets.UTF_8));
            digest.update(SEPARATOR);
            digest.update(request.getRequestURI().getBytes(StandardCharsets.UTF_8));
            digest.update(SEPARATOR);
            String queryString = request.getQueryString();
            if (queryString != null) {
                digest.update(queryString.getBytes(StandardCharsets.UTF_8));
            }
            digest.update(SEPARATOR);
            digest.update(body);
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 是 JDK 规范要求必须提供的算法，走到这里说明运行环境异常
            throw new IllegalStateException("SHA-256 摘要算法不可用", e);
        }
    }
}
