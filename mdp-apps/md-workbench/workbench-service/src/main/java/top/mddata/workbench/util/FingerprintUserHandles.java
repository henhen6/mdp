package top.mddata.workbench.util;

import com.yubico.webauthn.data.ByteArray;

import java.nio.charset.StandardCharsets;

/**
 * 指纹登录 WebAuthn userHandle 工具类。
 * <p>约定：WebAuthn username = 登录名（浏览器账号选择列表展示用，Repository 按 getByUsername 反查用户）；
 * displayName = 姓名。userHandle 即 userId 字符串的 UTF-8 字节，登录认证按 userHandle 还原 userId。</p>
 *
 * @author tangyh
 * @since 2026/11/13
 */
public final class FingerprintUserHandles {

    private FingerprintUserHandles() {
    }

    public static ByteArray userHandleOf(Long userId) {
        return new ByteArray(String.valueOf(userId).getBytes(StandardCharsets.UTF_8));
    }

    public static Long userIdOf(ByteArray userHandle) {
        return Long.valueOf(new String(userHandle.getBytes(), StandardCharsets.UTF_8));
    }
}
