package top.mddata.open.service.notify;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import top.mddata.base.constant.Constants;

import java.util.List;

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
     * 最大重试次数
     */
    private Integer maxRetry = 5;

    /**
     * 回调请求超时时间
     * 单位：毫秒
     */
    private int timeout = 5000;
    /**
     * 重试间隔
     * 对应第1，2，3...次尝试
     *      即5分钟后进行第一次尝试，如果失败，10分钟后进行第二次尝试
     *
     *      支持 m h d 三种单位，分别表示 分，时，天
     */
    private String timeLevel = "2m,5m,30m,6h,12h";
    /** 投递线程池核心线程数 */
    private Integer corePoolSize = 8;
    /** 投递线程池最大线程数 */
    private Integer maxPoolSize = 16;
    /** 投递线程池队列容量（满后拒绝，任务留待重试扫描兜底） */
    private Integer queueCapacity = 500;

    /**
     * 回调地址白名单（host 精确匹配，忽略大小写）。
     * 仅用于本地开发联调（如 127.0.0.1,localhost）；生产环境必须保持为空，否则 SSRF 防护被削弱
     */
    private List<String> allowedHosts = List.of();
}
