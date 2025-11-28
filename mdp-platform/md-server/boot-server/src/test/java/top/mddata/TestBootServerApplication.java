package top.mddata;

import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import top.mddata.base.validator.annotation.EnableFormValidator;
import top.mddata.common.ServerApplication;
import top.mddata.common.configuration.WebSocketConfig;

import java.net.UnknownHostException;


/**
 * 单体架构 启动类
 *
 * @author henhen6
 * @since 2025-06-24
 */
@SpringBootApplication
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {ServerEndpointExporter.class, WebSocketServletAutoConfiguration.class, WebSocketConfig.class}))
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@Slf4j
@EnableFormValidator
@EnableFileStorage
public class TestBootServerApplication extends ServerApplication {
    public static void main(String[] args) throws UnknownHostException {
        ServerApplication.start(TestBootServerApplication.class, args);
    }
}
