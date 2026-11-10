package top.mddata.base.idempotent.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import top.mddata.base.exception.BizException;
import top.mddata.base.idempotent.properties.IdempotencyProperties;
import top.mddata.base.idempotent.repository.IdempotencyRepository.CachedResponse;
import top.mddata.base.idempotent.support.IdempotencyResponseWriter;
import top.mddata.base.idempotent.support.IdempotencyConstants;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link IdempotencyRequestFilter} 与 {@link IdempotencyResponseWriter} 的行为测试。
 *
 * @author henhen6
 */
class IdempotencyRequestFilterTest {

    private IdempotencyProperties properties;
    private IdempotencyResponseWriter responseWriter;
    private IdempotencyRequestFilter filter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        properties = new IdempotencyProperties();
        responseWriter = new IdempotencyResponseWriter(objectMapper);
        filter = new IdempotencyRequestFilter(properties, responseWriter);
    }

    @Test
    void requestWithoutHeaderPassesThroughUnwrapped() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/organization/user/save");
        request.setContentType("application/json");
        request.setContent("{}".getBytes(StandardCharsets.UTF_8));
        RecordingFilterChain chain = new RecordingFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertEquals(1, chain.invocations);
        assertSame(request, chain.capturedRequest);
    }

    @Test
    void requestWithBlankHeaderPassesThroughUnwrapped() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/organization/user/save");
        request.addHeader(IdempotencyConstants.HEADER, "   ");
        request.setContentType("application/json");
        RecordingFilterChain chain = new RecordingFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertSame(request, chain.capturedRequest);
    }

    @Test
    void nonJsonRequestPassesThroughUnwrapped() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/organization/user/save");
        request.addHeader(IdempotencyConstants.HEADER, "token-1");
        request.setContentType("text/plain");
        RecordingFilterChain chain = new RecordingFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertEquals(1, chain.invocations);
        assertSame(request, chain.capturedRequest);
    }

    @Test
    void formRequestPassesThroughUnwrapped() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/organization/user/save");
        request.addHeader(IdempotencyConstants.HEADER, "token-1");
        request.setContentType("application/x-www-form-urlencoded");
        RecordingFilterChain chain = new RecordingFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertSame(request, chain.capturedRequest);
    }

    @Test
    void jsonRequestWithHeaderIsWrapped() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/organization/user/save");
        request.addHeader(IdempotencyConstants.HEADER, "token-1");
        request.setContentType("application/json;charset=UTF-8");
        request.setContent("{\"name\":\"张三\"}".getBytes(StandardCharsets.UTF_8));
        RecordingFilterChain chain = new RecordingFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertEquals(1, chain.invocations);
        assertNotSame(request, chain.capturedRequest);
        CachedBodyRequestWrapper wrapper = (CachedBodyRequestWrapper) chain.capturedRequest;
        assertEquals("{\"name\":\"张三\"}",
                new String(wrapper.getCachedBody(), StandardCharsets.UTF_8));
    }

    @Test
    void oversizedBodyReturns200WithFailBodyAndControllerNotInvoked() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/organization/user/save");
        request.addHeader(IdempotencyConstants.HEADER, "token-1");
        request.setContentType("application/json");
        request.setContent(new byte[33]);
        properties.setMaxRequestBody(org.springframework.util.unit.DataSize.ofBytes(32));
        RecordingFilterChain chain = new RecordingFilterChain();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(0, chain.invocations);
        assertEquals(200, response.getStatus());
        assertEquals("application/json", response.getContentType().replace(";charset=UTF-8", ""));
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals(-6, body.get("code").asInt());
        assertFalse(body.get("msg").asText().isBlank());
    }

    @Test
    void bizExceptionFromControllerIsNotConvertedByFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/organization/user/save");
        request.addHeader(IdempotencyConstants.HEADER, "token-1");
        request.setContentType("application/json");
        BizException fromController = new BizException(-10, "控制器内部业务失败");
        FilterChain failingChain = (req, res) -> {
            throw fromController;
        };

        BizException thrown = assertThrows(BizException.class,
                () -> filter.doFilter(request, new MockHttpServletResponse(), failingChain));
        assertSame(fromController, thrown);
    }

    @Test
    void replayRestoresStatusContentTypeAndBody() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        CachedResponse cached = new CachedResponse(202, "application/json;charset=UTF-8",
                "{\"code\":0,\"msg\":\"ok\"}");

        responseWriter.replay(response, cached);

        assertEquals(202, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        assertEquals("{\"code\":0,\"msg\":\"ok\"}", response.getContentAsString());
    }

    @Test
    void replayEncodesBodyWithCharsetDeclaredInContentType() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String body = "{\"city\":\"北京\"}";
        CachedResponse cached = new CachedResponse(200, "application/json;charset=GBK", body);

        responseWriter.replay(response, cached);

        assertEquals(200, response.getStatus());
        assertEquals("application/json;charset=GBK", response.getContentType());
        assertEquals(body, new String(response.getContentAsByteArray(),
                java.nio.charset.Charset.forName("GBK")));
    }

    @Test
    void replayDefaultsToUtf8WhenContentTypeHasNoCharset() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String body = "{\"city\":\"北京\"}";
        CachedResponse cached = new CachedResponse(200, "application/json", body);

        responseWriter.replay(response, cached);

        assertEquals(body, new String(response.getContentAsByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    void replayDoesNotIntroduceExtraHeaders() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        CachedResponse cached = new CachedResponse(200, "application/json", "{}");

        responseWriter.replay(response, cached);

        // Content-Type 由 setContentType 产生，除此之外不应复制或新增任何响应头
        assertEquals(1, response.getHeaderNames().size());
        assertEquals("application/json", response.getHeader("Content-Type"));
    }

    /** 记录下游是否被调用及收到的请求，避免用 mock 验证包装决策。 */
    private static class RecordingFilterChain implements FilterChain {
        private ServletRequest capturedRequest;
        private int invocations;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            this.capturedRequest = request;
            this.invocations++;
        }
    }
}
