package top.mddata.open.service.notify.pipeline;

import com.gitee.sop.support.aes.MdpBizMsgCrypt;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.mddata.open.service.notify.NotifyProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotifySenderTest {

    private HttpServer server;
    private NotifySender sender;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/ok", exchange -> respond(exchange, "{\"code\":0,\"msg\":\"\"}"));
        server.createContext("/bizFail", exchange -> respond(exchange, "{\"code\":1,\"msg\":\"no\"}"));
        server.createContext("/notJson", exchange -> respond(exchange, "not-json"));
        server.createContext("/empty", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            respond(exchange, "{\"code\":0,\"msg\":\"\"}");
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        NotifyProperties properties = new NotifyProperties();
        properties.setTimeout(500);
        sender = new NotifySender(properties);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private void respond(com.sun.net.httpserver.HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private NotifyTaskView newTask(String url) {
        return new NotifyTaskView() {
            @Override public Long getId() { return 1L; }
            @Override public String getNotifyUrl() { return url; }
            @Override public String getAppKey() { return "testAppKey"; }
            @Override public Integer getNotifyEncryptionType() { return 0; }
            @Override public String getNotifyToken() { return MdpBizMsgCrypt.generateToken(); }
            @Override public String getNotifyEncodingAesKey() { return MdpBizMsgCrypt.generateEncodingAesKey(); }
            @Override public Integer getRequestCnt() { return 1; }
            @Override public String getExecStatus() { return "0"; }
            @Override public String buildPlaintext() { return "{\"type\":\"CALLBACK\"}"; }
            @Override public void markDelivering(LocalDateTime now) { }
            @Override public void markStatus(String execStatus, LocalDateTime nextRequestTime) { }
        };
    }

    @Test
    void 对方返回code0判定成功() {
        NotifyResult result = sender.send(newTask(baseUrl + "/ok"));
        assertTrue(result.isSuccess());
        assertEquals("{\"code\":0,\"msg\":\"\"}", result.getResponseBody());
    }

    @Test
    void 对方返回业务失败码判定失败() {
        NotifyResult result = sender.send(newTask(baseUrl + "/bizFail"));
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMsg().contains("no"));
    }

    @Test
    void 对方返回非JSON判定失败() {
        NotifyResult result = sender.send(newTask(baseUrl + "/notJson"));
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMsg().contains("格式"));
    }

    @Test
    void 空响应体判定为格式错误() {
        NotifyResult result = sender.send(newTask(baseUrl + "/empty"));
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMsg().contains("格式错误"));
    }

    @Test
    void 超时判定失败且不抛异常() {
        NotifyResult result = sender.send(newTask(baseUrl + "/slow"));
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMsg().contains("请求异常"));
    }
}
