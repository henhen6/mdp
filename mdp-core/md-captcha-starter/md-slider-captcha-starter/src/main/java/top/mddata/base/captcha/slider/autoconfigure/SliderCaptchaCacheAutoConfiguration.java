package top.mddata.base.captcha.slider.autoconfigure;

import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.impl.CaptchaCacheServiceMemImpl;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import top.mddata.base.cache.RedisAutoConfigure;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.captcha.slider.autoconfigure.cache.SliderCaptchaCacheServiceImpl;
import top.mddata.base.captcha.slider.enumeration.StorageType;
import top.mddata.base.captcha.slider.properties.SliderCaptchaProperties;

/**
 * 行为验证码缓存自动配置
 *
 * @author henhen
 */
@AutoConfiguration
public class SliderCaptchaCacheAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SliderCaptchaCacheAutoConfiguration.class);

    private SliderCaptchaCacheAutoConfiguration() {
        log.info("加载行为验证码缓存自动配置");
    }

    /**
     * 自定义缓存实现-默认（内存）
     */
    @AutoConfiguration
    @ConditionalOnMissingBean(CaptchaCacheService.class)
    @ConditionalOnProperty(name = SliderCaptchaProperties.PREFIX + ".cache-type", havingValue = "default", matchIfMissing = true)
    public static class Default {
        @Bean
        public CaptchaCacheService captchaCacheService() {
            CaptchaCacheService service = new CaptchaCacheServiceMemImpl();
            CaptchaServiceFactory.cacheService.put(StorageType.DEFAULT.name().toLowerCase(), service);
            return service;
        }

    }

    /**
     * 自定义缓存实现-Redis
     */
    @AutoConfiguration(before = RedisAutoConfigure.class)
    @ConditionalOnClass(CacheOps.class)
    @ConditionalOnMissingBean(CaptchaCacheService.class)
    @ConditionalOnProperty(name = SliderCaptchaProperties.PREFIX + ".cache-type", havingValue = "redis")
    public static class Redis {
        @Bean
        public CaptchaCacheService captchaCacheService(CacheOps cacheOps) {
            SliderCaptchaCacheServiceImpl service = new SliderCaptchaCacheServiceImpl(cacheOps);
            CaptchaServiceFactory.cacheService.put(StorageType.REDIS.name().toLowerCase(), service);
            return service;
        }

    }

    /**
     * 自定义缓存实现
     */
    @AutoConfiguration
    @ConditionalOnProperty(name = SliderCaptchaProperties.PREFIX + ".cache-type", havingValue = "custom")
    public static class Custom {
        @Bean
        @ConditionalOnMissingBean
        public CaptchaCacheService captchaCacheService() {
            if (log.isErrorEnabled()) {
                log.error("请在配置中定义一个类型为'{}'的bean。", ResolvableType.forClass(CaptchaCacheService.class));
            }
            throw new NoSuchBeanDefinitionException(CaptchaCacheService.class);
        }

    }
}
