package top.mddata.common.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.core.datainterface.SmsReadConfig;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import top.mddata.common.properties.MsgProperties;

/**
 * 消息（站内信、短信、邮件相关）自动配置
 *
 * @author henhen
 * @date 2025年12月28日23:36:25
 */
@Configuration
@EnableConfigurationProperties({MsgProperties.class})
@RequiredArgsConstructor
@Slf4j
public class MsgAutoConfiguration {
    private final SmsReadConfig smsReadConfig;

    @PostConstruct
    public void init() {
        SmsFactory.createSmsBlend(smsReadConfig);
        log.info("[通过配置读取接口创建全部短信实例] 加载成功");
    }

}
