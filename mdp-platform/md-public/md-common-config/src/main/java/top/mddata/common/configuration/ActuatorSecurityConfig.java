package top.mddata.common.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 *
 * @author henhen6
 * @since 2025/10/24 20:34
 */
@Configuration
public class ActuatorSecurityConfig {

    // 1. 配置密码加密器（Spring Security 6 强制要求加密）
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 配置用户（生产环境建议用数据库存储，此处为示例用内存用户）
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // 仅授予 "ACTUATOR_ADMIN" 角色访问 Actuator
        UserDetails actuatorUser = User.withUsername("actuator-admin")
                .password(passwordEncoder.encode("Secure@2024"))  // 密码需复杂，生产环境避免硬编码
                .roles("ACTUATOR_ADMIN")
                .build();
        return new InMemoryUserDetailsManager(actuatorUser);
    }

    // 3. 配置 Actuator 端点权限规则
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF（若 Actuator 仅内部访问，可关闭；外部访问需评估）
                .csrf(csrf -> csrf.disable())
                // 配置请求权限
                .authorizeHttpRequests(auth -> auth
                        // Actuator 所有端点仅允许 "ACTUATOR_ADMIN" 角色访问
                        .requestMatchers("/actuator/**").hasRole("ACTUATOR_ADMIN")
                        // 其他业务接口根据需求配置（如允许匿名访问或其他角色）
                        .anyRequest().permitAll()
                )
                // 启用 HTTP Basic 认证（简单场景）；生产环境建议用 OAuth2/JWT 更安全
                .httpBasic(httpBasic -> {
                });

        return http.build();
    }
}
