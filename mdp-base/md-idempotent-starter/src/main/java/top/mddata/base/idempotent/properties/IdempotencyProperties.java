package top.mddata.base.idempotent.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import top.mddata.base.constant.Constants;

import java.time.Duration;

/**
 * 幂等组件配置。
 *
 * @author henhen6
 */
@Getter
@Setter
@ConfigurationProperties(IdempotencyProperties.PREFIX)
public class IdempotencyProperties {
    public static final String PREFIX = Constants.PROJECT_PREFIX + ".idempotent";

    /** 是否启用幂等保护。 */
    private boolean enabled = true;

    /** Redis key 前缀。 */
    private String keyPrefix = "mdp:idempotent:";

    /** 已签发但未使用令牌的存活时间。 */
    private Duration unusedTtl = Duration.ofMinutes(10);

    /** 请求处理中状态的存活时间。 */
    private Duration processingTtl = Duration.ofMinutes(10);

    /** 已完成响应缓存的存活时间。 */
    private Duration successTtl = Duration.ofHours(24);

    /** 允许幂等保护的最大请求体大小。 */
    private DataSize maxRequestBody = DataSize.ofMegabytes(1);
}
