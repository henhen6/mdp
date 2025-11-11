package top.mddata.base.log.properties;

import top.mddata.base.constant.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static top.mddata.base.log.properties.OptLogProperties.PREFIX;

/**
 * 操作日志配置类
 *
 * @author henhen6
 * @date 2020年03月09日15:00:47
 */
@ConfigurationProperties(prefix = PREFIX)
@Data
@NoArgsConstructor
public class OptLogProperties {
    public static final String PREFIX = Constants.PROJECT_PREFIX + ".log";

    /**
     * 是否启用
     */
    private Boolean enabled = true;
    private Mode mode = Mode.CLOUD;
    /**
     * 日志存储类型
     */
    private OptLogType type = OptLogType.DB;
}
