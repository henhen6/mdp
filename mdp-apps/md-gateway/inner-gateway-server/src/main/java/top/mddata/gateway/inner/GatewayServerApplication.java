package top.mddata.gateway.inner;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

import static top.mddata.base.constant.Constants.UTIL_PACKAGE;


/**
 * @author henhen
 * @date 2017-12-13 15:02
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class
})
@EnableDiscoveryClient
@ComponentScan(value = {
        UTIL_PACKAGE
})
@EnableFeignClients(value = {
        UTIL_PACKAGE
})
@Slf4j
public class GatewayServerApplication {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext application = SpringApplication.run(GatewayServerApplication.class, args);
        Environment env = application.getEnvironment();
        String msg = """
                
                ----------------------------------------------------------
                应用 '{}' 启动成功， JDK版本号：{} ！
                NextDoc4j 聚合文档: http://{}:{}{}/doc.html
                当前环境变量：{} 日志路径：{}
                ----------------------------------------------------------
                """;

        log.info(msg,
                env.getProperty("spring.application.name"),
                env.getProperty("java.version"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path", ""),
                env.getProperty("spring.profiles.active"),
                env.getProperty("LOG_PATH")
        );
    }
}
