package top.mddata.worker.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import top.mddata.open.service.admin.EventPushService;
import top.mddata.open.service.admin.NotifyInfoService;

import java.time.LocalDateTime;

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
    @Autowired
    private NotifyInfoService notifyInfoService;
    @Autowired
    private EventPushService eventPushService;
    public DubboConfiguration() {
        log.info("dubbo 已加载");
    }

    /**
     * 每10 分钟执行一次
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void runNotifyInfo() {
        notifyInfoService.retry(LocalDateTime.now());
    }

    /**
     * 每8分钟执行一次
     */
    @Scheduled(cron = "0 0/8 * * * ?")
    public void runEventPush() {
        eventPushService.retry(LocalDateTime.now());
    }

}
