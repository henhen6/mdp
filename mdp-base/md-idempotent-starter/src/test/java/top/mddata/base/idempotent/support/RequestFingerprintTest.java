package top.mddata.base.idempotent.support;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * {@link RequestFingerprint} 的行为测试。
 *
 * @author henhen6
 */
class RequestFingerprintTest {

    @Test
    void fingerprintBindsMethodPathQueryAndBody() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/organization/user/save");
        request.setQueryString("from=modal");
        byte[] body = "{\"name\":\"张三\"}".getBytes(StandardCharsets.UTF_8);

        String original = RequestFingerprint.sha256(request, body);
        request.setQueryString("from=other");

        assertNotEquals(original, RequestFingerprint.sha256(request, body));
    }

    @Test
    void sameRequestProducesStableFingerprint() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/organization/user/save");
        request.setQueryString("from=modal");
        byte[] body = "{\"name\":\"张三\"}".getBytes(StandardCharsets.UTF_8);

        assertEquals(RequestFingerprint.sha256(request, body), RequestFingerprint.sha256(request, body));
    }

    @Test
    void differentMethodProducesDifferentFingerprint() {
        byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest post = new MockHttpServletRequest("POST", "/organization/user/save");
        MockHttpServletRequest put = new MockHttpServletRequest("PUT", "/organization/user/save");

        assertNotEquals(RequestFingerprint.sha256(post, body), RequestFingerprint.sha256(put, body));
    }

    @Test
    void differentPathProducesDifferentFingerprint() {
        byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest save = new MockHttpServletRequest("POST", "/organization/user/save");
        MockHttpServletRequest delete = new MockHttpServletRequest("POST", "/organization/user/delete");

        assertNotEquals(RequestFingerprint.sha256(save, body),
                RequestFingerprint.sha256(delete, body));
    }

    @Test
    void differentBodyProducesDifferentFingerprint() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/organization/user/save");
        byte[] first = "{\"name\":\"张三\"}".getBytes(StandardCharsets.UTF_8);
        byte[] second = "{\"name\":\"李四\"}".getBytes(StandardCharsets.UTF_8);

        assertNotEquals(RequestFingerprint.sha256(request, first),
                RequestFingerprint.sha256(request, second));
    }

    @Test
    void nullQueryIsTreatedAsEmpty() {
        byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
        MockHttpServletRequest noQuery = new MockHttpServletRequest("POST", "/organization/user/save");
        MockHttpServletRequest emptyQuery = new MockHttpServletRequest("POST", "/organization/user/save");
        emptyQuery.setQueryString("");

        assertEquals(RequestFingerprint.sha256(noQuery, body),
                RequestFingerprint.sha256(emptyQuery, body));
    }

    @Test
    void chineseBodyDoesNotThrow() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/organization/user/save");
        byte[] body = "{\"name\":\"张三\"}".getBytes(StandardCharsets.UTF_8);
        assertDoesNotThrow(() -> RequestFingerprint.sha256(request, body));
    }
}
