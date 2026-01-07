package top.mddata.gateway.sop;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * 开放平台 - 接口网关 启动类
 *
 * @author henhen6
 * @since 2025-06-24
 */
@SpringBootApplication
@Slf4j
@EnableDubbo
public class SopGatewayServerApplication {
    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(SopGatewayServerApplication.class, args);
        Environment env = application.getEnvironment();
        String msg = """
                
                ----------------------------------------------------------
                > 应用 '{}' 启动成功!
                > 接口请求前缀: http://{}:{}{}/{}
                ----------------------------------------------------------
                """;
        log.info(msg,
                env.getProperty("spring.application.name"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path", ""),
                env.getProperty("mdp.gateway.path", "")
        );
    }
}
