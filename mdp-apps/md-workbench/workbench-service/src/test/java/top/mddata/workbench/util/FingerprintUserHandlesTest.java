package top.mddata.workbench.util;

import com.yubico.webauthn.data.ByteArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * userHandle 编解码测试
 *
 * @author tangyh
 * @since 2026/11/13
 */
class FingerprintUserHandlesTest {

    @Test
    @DisplayName("userId编码后可无损解码")
    void userId编码后可无损解码() {
        Long userId = 1234567890123456789L;
        ByteArray handle = FingerprintUserHandles.userHandleOf(userId);
        assertEquals(userId, FingerprintUserHandles.userIdOf(handle));
    }

    @Test
    @DisplayName("不同userId生成不同userHandle")
    void 不同userId生成不同userHandle() {
        var h1 = FingerprintUserHandles.userHandleOf(1L);
        var h2 = FingerprintUserHandles.userHandleOf(2L);
        assertEquals(false, h1.equals(h2));
    }
}
