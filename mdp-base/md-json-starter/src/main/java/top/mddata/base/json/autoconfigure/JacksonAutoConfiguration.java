package top.mddata.base.json.autoconfigure;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import top.mddata.base.json.module.DateJacksonModule;
import top.mddata.base.json.module.NumberJacksonModule;

import java.util.TimeZone;

/**
 * Jackson 自动配置
 *
 * @author henhen
 * @since 1.0.0
 */
@AutoConfigureBefore(org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class)
@PropertySource(value = "classpath:default-json-jackson.yml", factory = GeneralPropertySourceFactory.class)
public class JacksonAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JacksonAutoConfiguration.class);

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            DateJacksonModule javaTimeModule = new DateJacksonModule();
            SimpleModule bigNumberModule = new NumberJacksonModule();

            builder.timeZone(TimeZone.getDefault());
            builder.modules(javaTimeModule, bigNumberModule);
            log.debug("自动配置“Jackson”已完成初始化。");
        };
    }


}
