package top.mddata.open.admin.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import top.mddata.base.constant.Constants;

/**
 * 通知配置
 * @author henhen
 * @since 2026/1/2
 */
@Setter
@Getter
@RefreshScope
@ConfigurationProperties(prefix = NotifyProperties.PREFIX)
public class NotifyProperties {
    public static final String PREFIX = Constants.PROJECT_PREFIX + ".notify";

    /**
     * 重试间隔
     * 对应第1，2，3...次尝试
     *      即5分钟后进行第一次尝试，如果失败，10分钟后进行第二次尝试
     *
     *      支持 m h d 三种单位，分别表示 分，时，天
     */
    private String timeLevel = "5m,10m,30m,6h";
    /**
     * 回调请求超时时间
     * 单位：毫秒
     */
    private int timeout = 5000;
    /**
     * 重试线程数
     */
    private Integer threads = 8;
    /**
     * 每个线程跑几个任务
     */
    private Integer threadTasks = 50;
}
