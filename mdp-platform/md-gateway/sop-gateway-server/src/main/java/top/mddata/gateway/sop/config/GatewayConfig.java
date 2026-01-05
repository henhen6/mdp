package top.mddata.gateway.sop.config;

import com.gitee.sop.support.message.OpenMessageFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;


/**
 * @author 六如
 */
@Configuration
@Slf4j
public class GatewayConfig {
    @PostConstruct
    public void init() {
        OpenMessageFactory.initMessage();
    }
}
