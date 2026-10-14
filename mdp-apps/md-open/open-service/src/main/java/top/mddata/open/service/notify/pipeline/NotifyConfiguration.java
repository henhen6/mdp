package top.mddata.open.service.notify.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.mddata.open.service.notify.NotifyProperties;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 推送域装配：配置属性、重试策略、投递线程池统一在此创建
 * （修旧版 @EnableConfigurationProperties 挂在 ServiceImpl 上的隐式初始化链问题）。
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(NotifyProperties.class)
public class NotifyConfiguration {

    /** timeLevel 非法时启动即失败，避免运行期被静默当成"重试用尽" */
    @Bean
    public RetryPolicy retryPolicy(NotifyProperties notifyProperties) {
        return new RetryPolicy(notifyProperties.getTimeLevel());
    }

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolExecutor notifyTaskThreadPool(NotifyProperties properties) {
        AtomicInteger seq = new AtomicInteger(1);
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "notify-push-" + seq.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        };
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(properties.getQueueCapacity()),
                factory,
                new ThreadPoolExecutor.AbortPolicy());
        log.info("[推送] 投递线程池初始化, core={}, max={}, queue={}",
                properties.getCorePoolSize(), properties.getMaxPoolSize(), properties.getQueueCapacity());
        return pool;
    }
}
