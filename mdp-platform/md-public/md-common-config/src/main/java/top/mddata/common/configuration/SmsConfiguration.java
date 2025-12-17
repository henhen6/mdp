package top.mddata.common.configuration;

import org.dromara.email.jakarta.api.MailClient;
import org.dromara.email.jakarta.comm.config.MailSmtpConfig;
import org.dromara.email.jakarta.core.factory.MailFactory;
import org.dromara.sms4j.chuanglan.config.ChuangLanConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class SmsConfiguration {

    // 绑定 yaml 前缀 "sms.blends.chuanglan" 的配置到 BaseConfig 字段
    @Bean
    @ConfigurationProperties(prefix = "sms.blends.chuanglan")
    public ChuangLanConfig getChuangLanConfig() {
        return new ChuangLanConfig();
    }

    @Bean("qqMailClient")
    @Primary
    public MailClient getQqMailClient() {
        MailSmtpConfig config = MailSmtpConfig.builder()
                .port("465").isSSL("true").fromAddress("306479353@qq.com").smtpServer("smtp.qq.com").isAuth("true")
                .username("306479353@qq.com").password("vlttrdoxxfxpbiei").nickName("主数据平台")
                .build();
        //这里的key可以是任何可对比类型，用于后续从工厂取出邮件实现类用
        MailFactory.put("qq", config);
        return MailFactory.createMailClient("qq");
    }
}
