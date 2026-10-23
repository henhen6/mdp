package top.mddata.worker.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import static top.mddata.base.constant.Constants.UTIL_PACKAGE;

@ConditionalOnProperty(
        prefix = "dubbo",
        name = {"enabled"},
        havingValue = "true",
        matchIfMissing = true
)
@Configuration
@EnableDubbo(scanBasePackages = UTIL_PACKAGE)
@Slf4j
public class DubboConfiguration {

    public DubboConfiguration() {
        log.info("dubbo 已加载");
    }
}
