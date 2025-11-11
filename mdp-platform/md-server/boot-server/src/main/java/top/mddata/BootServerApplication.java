package top.mddata;

import top.mddata.common.ServerApplication;
import top.mddata.base.validator.annotation.EnableFormValidator;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.net.UnknownHostException;


/**
 * 单体架构 启动类
 *
 * @author henhen6
 * @since 2025-06-24
 */
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@Slf4j
@EnableFormValidator
@EnableFileStorage
public class BootServerApplication extends ServerApplication {
    public static void main(String[] args) throws UnknownHostException {
        ServerApplication.start(BootServerApplication.class, args);
    }
}
