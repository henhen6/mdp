package top.mddata.base.idempotent.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import top.mddata.base.exception.BizException;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link CachedBodyRequestWrapper} 的行为测试。
 *
 * @author henhen6
 */
class CachedBodyRequestWrapperTest {

    @Test
    void bodyCanBeReadTwice() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent("{\"username\":\"zhangsan\"}".getBytes(StandardCharsets.UTF_8));
        CachedBodyRequestWrapper wrapper = new CachedBodyRequestWrapper(request, 1024);

        String first = new String(wrapper.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String second = new String(wrapper.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(first, second);
        assertEquals(first, new String(wrapper.getCachedBody(), StandardCharsets.UTF_8));
    }

    @Test
    void oversizedBodyFailsFast() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(new byte[33]);
        assertThrows(BizException.class, () -> new CachedBodyRequestWrapper(request, 32));
    }

    @Test
    void oversizedBodyFailsWithDedicatedCode() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(new byte[33]);
        BizException exception = assertThrows(BizException.class,
                () -> new CachedBodyRequestWrapper(request, 32));
        assertEquals(-6, exception.getCode());
    }

    @Test
    void bodyExactlyAtLimitIsAccepted() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(new byte[32]);
        assertDoesNotThrow(() -> new CachedBodyRequestWrapper(request, 32));
    }

    @Test
    void emptyBodyCanBeReadTwice() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        CachedBodyRequestWrapper wrapper = new CachedBodyRequestWrapper(request, 32);

        assertEquals(0, wrapper.getInputStream().readAllBytes().length);
        assertEquals(0, wrapper.getInputStream().readAllBytes().length);
        assertEquals(0, wrapper.getCachedBody().length);
    }

    @Test
    void readerDefaultsToUtf8WhenCharsetMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent("{\"name\":\"张三\"}".getBytes(StandardCharsets.UTF_8));
        CachedBodyRequestWrapper wrapper = new CachedBodyRequestWrapper(request, 1024);

        assertEquals("{\"name\":\"张三\"}", readAll(wrapper.getReader()));
    }

    @Test
    void readerUsesDeclaredRequestCharset() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        request.setContent("{\"city\":\"北京\"}".getBytes(StandardCharsets.UTF_8));
        CachedBodyRequestWrapper wrapper = new CachedBodyRequestWrapper(request, 1024);

        assertEquals("{\"city\":\"北京\"}", readAll(wrapper.getReader()));
    }

    private static String readAll(BufferedReader reader) {
        return reader.lines().reduce("", String::concat);
    }
}
