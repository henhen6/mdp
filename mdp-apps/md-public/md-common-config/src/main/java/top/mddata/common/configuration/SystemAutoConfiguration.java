package top.mddata.common.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.mddata.common.aspect.MethodLogAspect;
import top.mddata.common.configurer.AlwaysConfigurer;
import top.mddata.common.properties.MsgProperties;
import top.mddata.common.properties.SystemProperties;

/**
 * @author henhen6
 * @version v1.0
 * @since 2021/9/5 8:04 下午
 * @create [2021/9/5 8:04 下午 ] [henhen6] [初始创建]
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnWebApplication
@EnableConfigurationProperties({MsgProperties.class, SystemProperties.class})
public class SystemAutoConfiguration {


    @Bean
    public AlwaysConfigurer getAlwaysConfigurer(SystemProperties systemProperties) {
        return new AlwaysConfigurer(systemProperties);
    }

    /**
     * 方法日志切面。不按 recordLog 做条件注册：SystemProperties 是 @RefreshScope 代理，
     * 运行时开关判断可支持 Nacos 动态刷新；关闭时切面仅一次布尔判断，开销可忽略
     */
    @Bean
    public MethodLogAspect methodLogAspect(SystemProperties systemProperties) {
        return new MethodLogAspect(systemProperties);
    }

}
