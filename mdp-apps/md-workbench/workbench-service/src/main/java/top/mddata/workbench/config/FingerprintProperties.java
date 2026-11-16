package top.mddata.workbench.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 指纹登录配置
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Data
@Component
@ConfigurationProperties(prefix = "mdp.fingerprint")
public class FingerprintProperties {
    /** 是否启用指纹登录 */
    private Boolean enabled = false;
    /** RP ID：必须等于登录页部署域名（或其父域），启用时必填 */
    private String rpId;
    /** RP 显示名称 */
    private String rpName = "MDP主数据平台";
    /** 允许的前端 origin，严格匹配 */
    private Set<String> origins = new HashSet<>();
    /** 单用户最大凭证数 */
    private Integer maxCredentialsPerUser = 10;
}
