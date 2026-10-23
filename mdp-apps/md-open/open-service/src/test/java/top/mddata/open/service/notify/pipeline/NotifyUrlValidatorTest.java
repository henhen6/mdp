package top.mddata.open.service.notify.pipeline;

import com.gitee.sop.support.exception.OpenException;
import org.junit.jupiter.api.Test;
import top.mddata.open.service.notify.NotifyProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotifyUrlValidatorTest {

    private NotifyUrlValidator newValidator(String... allowedHosts) {
        NotifyProperties properties = new NotifyProperties();
        properties.setAllowedHosts(List.of(allowedHosts));
        return new NotifyUrlValidator(properties);
    }

    @Test
    void 合法地址通过() {
        NotifyUrlValidator validator = newValidator();
        assertDoesNotThrow(() -> validator.validate("https://example.com/notify"));
        assertDoesNotThrow(() -> validator.validate("http://8.8.8.8:8080/callback"));
    }

    @Test
    void 空地址与非法协议被拒绝() {
        NotifyUrlValidator validator = newValidator();
        assertThrows(OpenException.class, () -> validator.validate(null));
        assertThrows(OpenException.class, () -> validator.validate("  "));
        assertThrows(OpenException.class, () -> validator.validate("ftp://example.com/x"));
        assertThrows(OpenException.class, () -> validator.validate("not-a-url"));
    }

    @Test
    void 内网地址被拒绝() {
        NotifyUrlValidator validator = newValidator();
        assertThrows(OpenException.class, () -> validator.validate("http://127.0.0.1:8080/x"));
        assertThrows(OpenException.class, () -> validator.validate("http://localhost/x"));
        assertThrows(OpenException.class, () -> validator.validate("http://192.168.1.10/x"));
        assertThrows(OpenException.class, () -> validator.validate("http://10.0.0.5/x"));
        assertThrows(OpenException.class, () -> validator.validate("http://[::1]/x"));
        assertThrows(OpenException.class, () -> validator.validate("http://0.0.0.0/x"));
        assertThrows(OpenException.class, () -> validator.validate("http://172.16.0.1/x"));
        assertThrows(OpenException.class, () -> validator.validate("http://[::ffff:7f00:1]/x"));
        assertThrows(OpenException.class, () -> validator.validate("http://[::ffff:192.168.1.1]/x"));
        assertThrows(OpenException.class, () -> validator.validate("http://[fc00::1]/x"));
        assertThrows(OpenException.class, () -> validator.validate("http://[fe80::1]/x"));
    }

    @Test
    void 白名单内地址直接放行() {
        NotifyUrlValidator validator = newValidator("127.0.0.1", "localhost");
        // 白名单精确匹配（忽略大小写）即放行
        assertDoesNotThrow(() -> validator.validate("http://127.0.0.1:18080/callback/notify"));
        assertDoesNotThrow(() -> validator.validate("http://LOCALHOST/x"));
        // 不在白名单的内网地址仍被拦截
        assertThrows(OpenException.class, () -> validator.validate("http://192.168.1.10/x"));
        assertThrows(OpenException.class, () -> validator.validate("http://10.0.0.5/x"));
    }
}
