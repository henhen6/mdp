package top.mddata.workbench.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import top.mddata.common.entity.User;
import top.mddata.workbench.entity.FingerprintCredential;
import top.mddata.workbench.mapper.FingerprintCredentialMapper;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.util.FingerprintUserHandles;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Yubico CredentialRepository 适配器：以 mdw_fingerprint_credential 为凭证源。
 * <p>约定：WebAuthn username = 登录名，按 {@code SsoUserService.getByUsername} 反查用户；
 * userHandle = 用户ID字符串的 UTF-8 字节（见 {@link FingerprintUserHandles}）。</p>
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Repository
@RequiredArgsConstructor
public class FingerprintCredentialRepositoryImpl implements CredentialRepository {
    private final FingerprintCredentialMapper fingerprintCredentialMapper;
    private final SsoUserService ssoUserService;

    /**
     * 按 credentialId(base64url) 查询有效凭证（逻辑删除的由全局 logicDelete 过滤）
     */
    public FingerprintCredential getByCredentialId(String credentialId) {
        return fingerprintCredentialMapper.selectOneByQuery(
                QueryWrapper.create().eq(FingerprintCredential::getCredentialId, credentialId));
    }

    public List<FingerprintCredential> listByUserId(Long userId) {
        return fingerprintCredentialMapper.selectListByQuery(
                QueryWrapper.create().eq(FingerprintCredential::getUserId, userId));
    }

    private Optional<FingerprintCredential> findRecord(ByteArray credentialId) {
        return Optional.ofNullable(getByCredentialId(credentialId.getBase64Url()));
    }

    private RegisteredCredential toRegistered(FingerprintCredential record) {
        return RegisteredCredential.builder()
                .credentialId(new ByteArray(Base64.getUrlDecoder()
                        .decode(record.getCredentialId())))
                .userHandle(FingerprintUserHandles.userHandleOf(record.getUserId()))
                .publicKeyCose(new ByteArray(Base64.getDecoder().decode(record.getPublicKey())))
                .signatureCount(record.getSignCount() == null ? 0L : record.getSignCount())
                .build();
    }

    private Set<PublicKeyCredentialDescriptor> descriptors(List<FingerprintCredential> records) {
        return records.stream()
                .map(r -> PublicKeyCredentialDescriptor.builder()
                        .id(new ByteArray(Base64.getUrlDecoder().decode(r.getCredentialId())))
                        .type(PublicKeyCredentialType.PUBLIC_KEY)
                        .build())
                .collect(Collectors.toSet());
    }

    private Optional<Long> resolveUserId(String username) {
        User user = ssoUserService.getByUsername(username);
        return user == null ? Optional.empty() : Optional.of(user.getId());
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        // 约定：username 即登录名，用于 startRegistration 自动填充 excludeCredentials
        return resolveUserId(username)
                .map(userId -> descriptors(listByUserId(userId)))
                .orElseGet(Collections::emptySet);
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        return resolveUserId(username).map(FingerprintUserHandles::userHandleOf);
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        User user = ssoUserService.getById(FingerprintUserHandles.userIdOf(userHandle));
        return user == null ? Optional.empty() : Optional.ofNullable(user.getUsername());
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return findRecord(credentialId)
                .filter(r -> FingerprintUserHandles.userHandleOf(r.getUserId()).equals(userHandle))
                .map(this::toRegistered);
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        return findRecord(credentialId)
                .map(r -> Collections.singleton(toRegistered(r)))
                .orElseGet(Collections::emptySet);
    }
}
