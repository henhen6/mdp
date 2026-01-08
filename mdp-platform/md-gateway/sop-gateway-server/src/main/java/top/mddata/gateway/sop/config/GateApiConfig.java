package top.mddata.gateway.sop.config;

import com.gitee.sop.support.dto.ApiConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import top.mddata.base.constant.Constants;

/**
 * @author henhen
 */
@Configuration
@ConfigurationProperties(prefix = Constants.PROJECT_PREFIX + ".api")
public class GateApiConfig extends ApiConfig {
}
