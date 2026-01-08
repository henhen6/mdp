package top.mddata.console.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import top.mddata.open.admin.service.NotifyInfoService;

import java.time.LocalDateTime;

import static top.mddata.base.constant.Constants.UTIL_PACKAGE;

@Configuration
// 仅当 spring.profiles.active = prod 时，该配置类才会被加载
@Profile({"prod", "test", "sop"})
//@ConditionalOnClass(DubboConfigConfiguration.class)
@EnableDubbo(scanBasePackages = UTIL_PACKAGE)
@Slf4j
public class DubboConfiguration {
    public DubboConfiguration() {
        log.info("dubbo 已加载");
    }


    @Autowired
    private NotifyInfoService notifyInfoService;

    /**
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void run() {
        notifyInfoService.retry(LocalDateTime.now());
    }

}
