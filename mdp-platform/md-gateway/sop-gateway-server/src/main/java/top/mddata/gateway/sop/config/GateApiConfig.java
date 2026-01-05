package top.mddata.gateway.sop.config;

import com.gitee.sop.support.dto.ApiConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author henhen
 */
@Configuration
@ConfigurationProperties(prefix = "api")
public class GateApiConfig extends ApiConfig {
}
