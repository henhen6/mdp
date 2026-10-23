package top.mddata.gateway.sop.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 接口调用日志异步写入配置。
 * 调用日志是旁观型数据：可丢、不可影响业务响应，故队列有界且满则丢弃。
 */
@Slf4j
@Configuration
@EnableAsync
public class CallLogAsyncConfiguration {

    @Bean("apiCallLogExecutor")
    public TaskExecutor apiCallLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("api-call-log-");
        executor.setRejectedExecutionHandler((runnable, pool) ->
                log.warn("调用日志写入队列已满，丢弃一条日志记录"));
        executor.initialize();
        return executor;
    }
}
