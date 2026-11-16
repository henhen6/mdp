package top.mddata.workbench.config;

import cn.hutool.core.util.StrUtil;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.workbench.service.impl.FingerprintCredentialRepositoryImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * WebAuthn RelyingParty 装配
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Configuration
@RequiredArgsConstructor
public class FingerprintWebAuthnConfig {
    private final FingerprintProperties fingerprintProperties;
    private final FingerprintCredentialRepositoryImpl credentialRepository;

    @Bean
    public RelyingParty relyingParty() {
        boolean enabled = Boolean.TRUE.equals(fingerprintProperties.getEnabled());
        // 启动期校验：仅在指纹登录启用时强制要求 rp-id/origins 配置
        if (enabled) {
            ArgumentAssert.notEmpty(fingerprintProperties.getRpId(),
                    "指纹登录已启用，请配置 mdp.fingerprint.rp-id");
            ArgumentAssert.notEmpty(fingerprintProperties.getOrigins(),
                    "指纹登录已启用，请配置 mdp.fingerprint.origins");
        }

        // 未启用时给占位值保证 bean 可创建（不会被实际调用）；RelyingParty.origins 不接受空集合
        Set<String> origins = fingerprintProperties.getOrigins();
        Set<String> effectiveOrigins = origins.isEmpty()
                ? new HashSet<>(List.of("http://localhost:5173"))
                : origins;

        return RelyingParty.builder()
                .identity(RelyingPartyIdentity.builder()
                        // 未启用时给占位值保证 bean 可创建（不会被实际调用）
                        .id(StrUtil.blankToDefault(fingerprintProperties.getRpId(), "localhost"))
                        .name(fingerprintProperties.getRpName())
                        .build())
                .credentialRepository(credentialRepository)
                .origins(effectiveOrigins)
                .build();
    }
}
