package top.mddata.base.scan.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.mddata.base.constant.Constants;

/**
 * 缓存配置
 *
 * @author henhen6
 * @date 2021年09月19日15:12:32
 */
@Data
@ConfigurationProperties(prefix = ScanProperties.PREFIX)
public class ScanProperties {
    public static final String PREFIX = Constants.PROJECT_PREFIX + ".scan";

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 扫描包路径
     */
    private String basePackage;
}
