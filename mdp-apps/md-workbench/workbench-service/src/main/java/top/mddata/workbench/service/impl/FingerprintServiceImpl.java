package top.mddata.workbench.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybatisflex.core.query.QueryWrapper;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialParameters;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.mddata.base.base.R;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.BizException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.util.ContextUtil;
import top.mddata.common.cache.workbench.FingerprintChallengeCacheKeyBuilder;
import top.mddata.common.constant.DefValConstants;
import top.mddata.common.cache.workbench.FingerprintPwdVerifiedCacheKeyBuilder;
import top.mddata.common.entity.User;
import top.mddata.workbench.config.FingerprintProperties;
import top.mddata.workbench.dto.FingerprintLoginVerifyDto;
import top.mddata.workbench.dto.FingerprintRegisterVerifyDto;
import top.mddata.workbench.dto.FingerprintRenameDto;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.LoginLogDto;
import top.mddata.workbench.entity.FingerprintCredential;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.event.LoginEvent;
import top.mddata.workbench.handler.LoginStrategy;
import top.mddata.workbench.mapper.FingerprintCredentialMapper;
import top.mddata.workbench.service.FingerprintService;
import top.mddata.workbench.service.LoginSessionService;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.util.FingerprintUserHandles;
import top.mddata.workbench.vo.FingerprintCredentialVo;
import top.mddata.workbench.vo.FingerprintLoginOptionsVo;
import top.mddata.workbench.vo.FingerprintRegisterOptionsVo;
import top.mddata.workbench.vo.LoginVo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 指纹登录服务实现
 * <p>基于 Yubico webauthn-server-core：录入（WebAuthn 注册）与登录（WebAuthn 认证）挑战均一次性消费，
 * TTL 由 {@link FingerprintChallengeCacheKeyBuilder} 控制（5 分钟）。</p>
 * <p>约定：WebAuthn username = 登录名（浏览器账号选择列表展示用）；userHandle = 用户ID字符串的
 * UTF-8 字节（见 {@link FingerprintUserHandles}）。</p>
 *
 * @author tangyh
 * @since 2026/11/13
 */
@Slf4j
@Service
public class FingerprintServiceImpl extends SuperServiceImpl<FingerprintCredentialMapper, FingerprintCredential>
        implements FingerprintService {

    /** User-Agent 截断最大长度 */
    private static final int MAX_USER_AGENT_LENGTH = 500;

    private final RelyingParty relyingParty;
    private final FingerprintProperties fingerprintProperties;
    private final SsoUserService ssoUserService;
    /** 指纹认证通过后签发会话 */
    private final LoginSessionService loginSessionService;
    private final Map<String, LoginStrategy> loginStrategy;

    public FingerprintServiceImpl(RelyingParty relyingParty, FingerprintProperties fingerprintProperties,
                                  SsoUserService ssoUserService, LoginSessionService loginSessionService,
                                  Map<String, LoginStrategy> loginStrategy,
                                  FingerprintCredentialMapper mapper, CacheOps cacheOps) {
        this.relyingParty = relyingParty;
        this.fingerprintProperties = fingerprintProperties;
        this.ssoUserService = ssoUserService;
        this.loginSessionService = loginSessionService;
        this.loginStrategy = loginStrategy;
        // mapper/cacheOps 为父类保护字段（mybatisflex ServiceImpl#mapper、SuperServiceImpl#cacheOps），
        // 构造器注入保证纯单测可直接替换为 mock
        this.mapper = mapper;
        this.cacheOps = cacheOps;
    }

    @Override
    public R<Boolean> checkPassword(String password) {
        Long userId = ContextUtil.getUserId();
        if (userId == null) {
            throw new BizException("请先登录");
        }
        User user = ssoUserService.getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        LoginDto loginDto = new LoginDto();
        loginDto.setAuthType(AuthTypeEnum.USERNAME);
        loginDto.setUsername(user.getUsername());
        loginDto.setPassword(password);
        // 复用用户名密码校验（含密码错误计数与锁定逻辑）
        LoginStrategy strategy = loginStrategy.get(AuthTypeEnum.USERNAME.name());
        if (strategy == null) {
            throw new BizException("登录策略未配置: USERNAME");
        }
        strategy.checkUserPassword(loginDto, user);
        // 发一次性票据：registrationOptions 强制消费，防止持有登录态直接绕过密码验证录入公钥
        cacheOps.set(new FingerprintPwdVerifiedCacheKeyBuilder().key(String.valueOf(userId)), "1");
        return R.success(Boolean.TRUE);
    }

    @Override
    public FingerprintRegisterOptionsVo registrationOptions() {
        if (!Boolean.TRUE.equals(fingerprintProperties.getEnabled())) {
            throw new BizException("指纹登录未启用");
        }
        Long userId = ContextUtil.getUserId();
        if (userId == null) {
            throw new BizException("请先登录");
        }
        // 一次性消费密码验证票据：checkPassword 通过后 5 分钟内有效
        CacheKey pwdKey = new FingerprintPwdVerifiedCacheKeyBuilder().key(String.valueOf(userId));
        CacheResult<String> pwdTicket = cacheOps.get(pwdKey);
        cacheOps.del(pwdKey);
        if (pwdTicket == null || StrUtil.isEmpty(pwdTicket.getValue())) {
            throw new BizException("请先验证登录密码");
        }

        User user = ssoUserService.getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        // startRegistration 自动生成随机 challenge，并自动 excludeCredentials 已注册凭证
        PublicKeyCredentialCreationOptions options = relyingParty.startRegistration(
                StartRegistrationOptions.builder()
                        .user(buildUserIdentity(user, userId))
                        .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                                .residentKey(ResidentKeyRequirement.REQUIRED)
                                .userVerification(UserVerificationRequirement.REQUIRED)
                                .build())
                        .build());

        FingerprintRegisterOptionsVo vo = new FingerprintRegisterOptionsVo();
        vo.setChallengeId(saveChallenge(options.getChallenge().getBase64Url()));
        try {
            vo.setOptionsJson(options.toCredentialsCreateJson());
        } catch (JsonProcessingException e) {
            log.warn("生成指纹录入挑战JSON失败: {}", e.getMessage());
            throw new BizException("生成录入挑战失败，请重试");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FingerprintCredentialVo verifyRegistration(FingerprintRegisterVerifyDto dto) {
        Long userId = ContextUtil.getUserId();
        if (userId == null) {
            throw new BizException("请先登录");
        }
        String challenge = getAndDeleteChallenge(dto.getChallengeId());
        if (StrUtil.isEmpty(challenge)) {
            throw new BizException("录入请求已过期，请重新录入");
        }
        long count = mapper.selectCountByQuery(
                QueryWrapper.create().eq(FingerprintCredential::getUserId, userId));
        if (count >= fingerprintProperties.getMaxCredentialsPerUser()) {
            throw new BizException("指纹数量已达上限，请先删除部分设备");
        }
        User user = ssoUserService.getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        // 重建注册请求，供 finishRegistration 校验 challenge 与响应的一致性
        ByteArray challengeBytes;
        try {
            challengeBytes = ByteArray.fromBase64Url(challenge);
        } catch (Base64UrlException e) {
            log.warn("指纹录入challenge解码失败: {}", e.getMessage());
            throw new BizException("录入请求已过期，请重新录入");
        }
        PublicKeyCredentialCreationOptions request = PublicKeyCredentialCreationOptions.builder()
                .rp(RelyingPartyIdentity.builder()
                        .id(fingerprintProperties.getRpId())
                        .name(fingerprintProperties.getRpName())
                        .build())
                .user(buildUserIdentity(user, userId))
                .challenge(challengeBytes)
                .pubKeyCredParams(List.of(PublicKeyCredentialParameters.ES256,
                        PublicKeyCredentialParameters.RS256))
                .timeout(60_000L)
                .excludeCredentials(descriptors(listRecordsByUserId(userId)))
                .authenticatorSelection(AuthenticatorSelectionCriteria.builder()
                        .residentKey(ResidentKeyRequirement.REQUIRED)
                        .userVerification(UserVerificationRequirement.REQUIRED)
                        .build())
                .build();

        try {
            RegistrationResult result = relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                    .request(request)
                    .response(PublicKeyCredential.parseRegistrationResponseJson(dto.getResponseJson()))
                    .build());

            FingerprintCredential record = new FingerprintCredential();
            record.setUserId(userId);
            record.setCredentialId(result.getKeyId().getId().getBase64Url());
            record.setPublicKey(result.getPublicKeyCose().getBase64());
            record.setSignCount(result.getSignatureCount());
            record.setAaguid(aaguidToString(result.getAaguid()));
            record.setDeviceId(dto.getDeviceId());
            record.setDeviceName(dto.getDeviceName());
            record.setPlatform(dto.getPlatform());
            record.setUserAgent(currentUa());
            record.setRegisteredAt(LocalDateTime.now());
            mapper.insert(record);
            return toVo(record);
        } catch (RegistrationFailedException | IOException | DuplicateKeyException
                 | IllegalArgumentException e) {
            log.warn("指纹录入验证失败: ", e);
            throw new BizException("指纹录入失败：该设备可能已录入过指纹，或验证未通过，请重试");
        }
    }

    @Override
    public List<FingerprintCredentialVo> listMine() {
        return toVo(listRecordsByUserId(ContextUtil.getUserId()));
    }

    @Override
    public Boolean rename(Long id, FingerprintRenameDto dto) {
        FingerprintCredential record = mapper.selectOneById(id);
        if (record == null) {
            throw new BizException("指纹凭证不存在");
        }
        checkOwner(record);
        record.setDeviceName(dto.getName());
        return updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        FingerprintCredential record = mapper.selectOneById(id);
        if (record == null) {
            throw new BizException("指纹凭证不存在");
        }
        checkOwner(record);
        // removeById 继承自 SuperService，走全局逻辑删除（deleted_at）
        return removeById(id);
    }

    @Override
    public FingerprintLoginOptionsVo loginOptions() {
        if (!Boolean.TRUE.equals(fingerprintProperties.getEnabled())) {
            throw new BizException("指纹登录未启用");
        }
        AssertionRequest request = relyingParty.startAssertion(StartAssertionOptions.builder()
                .userVerification(UserVerificationRequirement.REQUIRED)
                .build());

        FingerprintLoginOptionsVo vo = new FingerprintLoginOptionsVo();
        // 缓存完整的 AssertionRequest JSON，loginVerify 时原样恢复（2.8.0 不支持重建时注入 challenge）
        try {
            vo.setChallengeId(saveChallenge(request.toJson()));
            vo.setOptionsJson(request.toCredentialsGetJson());
        } catch (JsonProcessingException e) {
            log.warn("生成指纹登录挑战JSON失败: {}", e.getMessage());
            throw new BizException("生成登录挑战失败，请重试");
        }
        return vo;
    }

    @Override
    public R<LoginVo> loginVerify(FingerprintLoginVerifyDto dto) {
        String requestJson = getAndDeleteChallenge(dto.getChallengeId());
        if (StrUtil.isEmpty(requestJson)) {
            throw new BizException("登录请求已过期，请重新验证");
        }
        AssertionRequest request;
        try {
            request = AssertionRequest.fromJson(requestJson);
        } catch (JsonProcessingException e) {
            log.warn("恢复指纹登录挑战失败: {}", e.getMessage());
            throw new BizException("登录请求已过期，请重新验证");
        }

        try {
            AssertionResult result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                    .request(request)
                    .response(PublicKeyCredential.parseAssertionResponseJson(dto.getResponseJson()))
                    .build());
            if (!result.isSuccess() || !result.isSignatureCounterValid()) {
                publishLoginFail(result);
                throw new BizException("指纹验证失败");
            }

            Long userId = FingerprintUserHandles.userIdOf(result.getUserHandle());
            FingerprintCredential record =
                    getByCredentialId(result.getCredentialId().getBase64Url());
            if (record == null || !userId.equals(record.getUserId())) {
                throw new BizException("指纹验证失败");
            }

            record.setSignCount(result.getSignatureCount());
            record.setLastUsedAt(LocalDateTime.now());
            record.setLastLoginIp(currentIp());
            updateById(record);

            User user = ssoUserService.getById(userId);
            if (user == null) {
                throw new BizException("用户不存在");
            }
            checkUserEnabled(user);

            LoginVo loginVo = loginSessionService.loginAuthenticated(user, AuthTypeEnum.FINGERPRINT,
                    user.getUsername(), dto.getDeviceId(), dto.getDeviceInfo(), "PC");
            return R.success(loginVo);
        } catch (AssertionFailedException | IOException | IllegalArgumentException e) {
            log.warn("指纹登录验证失败: {}", e.getMessage());
            throw new BizException("指纹验证失败");
        }
    }

    // ===== 私有方法 =====

    /**
     * 发布指纹登录失败事件（异常不影响主流程，仅 debug 记录）
     */
    private void publishLoginFail(AssertionResult result) {
        try {
            Long userId = FingerprintUserHandles.userIdOf(result.getUserHandle());
            // 按 userHandle 还原账号，查不到时传 null，由日志监听器兜底处理
            User user = ssoUserService.getById(userId);
            String account = user == null ? null : user.getUsername();
            LoginLogDto failDto = LoginLogDto.failByCheck(
                            AuthTypeEnum.FINGERPRINT, null, account, "指纹验证失败")
                    .setAppKey(DefValConstants.WORKBENCH_APP_KEY)
                    .setAppName(DefValConstants.WORKBENCH_APP_NAME);
            SpringUtil.publishEvent(new LoginEvent(failDto));
        } catch (Exception e) {
            log.debug("指纹登录失败事件发布失败: {}", e.getMessage());
        }
    }

    /**
     * 检测用户状态是否正常（禁用时先发布失败事件再拒绝，
     * state 语义 [false-禁用 true-正常]，null 视为禁用，
     * 与 UsernameLoginStrategyImpl.checkUserState 一致）
     */
    private void checkUserEnabled(User user) {
        if (user.getState() == null || !user.getState()) {
            String msg = "您已被禁用，请联系管理员开通账号！";
            LoginLogDto dto = LoginLogDto.failByCheck(
                            AuthTypeEnum.FINGERPRINT, null, user.getUsername(), msg)
                    .setAppKey(DefValConstants.WORKBENCH_APP_KEY)
                    .setAppName(DefValConstants.WORKBENCH_APP_NAME);
            SpringUtil.publishEvent(new LoginEvent(dto));
            throw new BizException(msg);
        }
    }

    private FingerprintCredential getByCredentialId(String credentialId) {
        return mapper.selectOneByQuery(
                QueryWrapper.create().eq(FingerprintCredential::getCredentialId, credentialId));
    }

    private List<FingerprintCredential> listRecordsByUserId(Long userId) {
        return mapper.selectListByQuery(
                QueryWrapper.create().eq(FingerprintCredential::getUserId, userId));
    }

    private Set<PublicKeyCredentialDescriptor> descriptors(List<FingerprintCredential> records) {
        return records.stream().map(r -> {
            try {
                return PublicKeyCredentialDescriptor.builder()
                        .id(ByteArray.fromBase64Url(r.getCredentialId()))
                        .type(PublicKeyCredentialType.PUBLIC_KEY)
                        .build();
            } catch (Base64UrlException e) {
                throw new IllegalArgumentException(e);
            }
        }).collect(Collectors.toSet());
    }

    private UserIdentity buildUserIdentity(User user, Long userId) {
        return UserIdentity.builder()
                // WebAuthn username = 登录名（浏览器账号选择列表按此展示）；
                // displayName = 姓名。Repository 按 getByUsername(username) 反查 userId，
                // userHandle 仍为 userId 字符串的 UTF-8 字节（见 FingerprintUserHandles）
                .name(user.getUsername())
                .displayName(StrUtil.blankToDefault(user.getName(), user.getUsername()))
                .id(FingerprintUserHandles.userHandleOf(userId))
                .build();
    }

    private FingerprintCredentialVo toVo(FingerprintCredential record) {
        FingerprintCredentialVo vo = new FingerprintCredentialVo();
        vo.setId(record.getId());
        vo.setDeviceName(record.getDeviceName());
        vo.setPlatform(record.getPlatform());
        vo.setAaguid(record.getAaguid());
        vo.setRegisteredAt(record.getRegisteredAt());
        vo.setLastUsedAt(record.getLastUsedAt());
        vo.setLastLoginIp(record.getLastLoginIp());
        return vo;
    }

    private List<FingerprintCredentialVo> toVo(List<FingerprintCredential> records) {
        return records.stream().map(this::toVo).toList();
    }

    private void checkOwner(FingerprintCredential record) {
        // 越权访问属于业务拒绝，用 BizException 保证前端响应格式统一
        if (!record.getUserId().equals(ContextUtil.getUserId())) {
            throw new BizException("无权操作他人指纹凭证");
        }
    }

    /**
     * 保存挑战（一次性消费缓存），返回挑战ID
     */
    private String saveChallenge(String value) {
        String challengeId = IdUtil.fastSimpleUUID();
        cacheOps.set(new FingerprintChallengeCacheKeyBuilder().key(challengeId), value);
        return challengeId;
    }

    /**
     * 取出并删除挑战（无论取值成败均删除，保证一次性消费）
     */
    private String getAndDeleteChallenge(String challengeId) {
        if (StrUtil.isEmpty(challengeId)) {
            return null;
        }
        CacheKey key = new FingerprintChallengeCacheKeyBuilder().key(challengeId);
        CacheResult<String> result = cacheOps.get(key);
        String value = result == null ? null : result.getValue();
        cacheOps.del(key);
        return value;
    }

    /**
     * AAGUID ByteArray(16字节) 转 UUID 标准格式字符串，异常情况返回 null
     */
    private String aaguidToString(ByteArray aaguid) {
        if (aaguid == null) {
            return null;
        }
        String hex = aaguid.getHex();
        if (hex.length() != 32) {
            return null;
        }
        return String.format("%s-%s-%s-%s-%s",
                hex.substring(0, 8), hex.substring(8, 12), hex.substring(12, 16),
                hex.substring(16, 20), hex.substring(20, 32));
    }

    private String currentUa() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return "";
        }
        return StrUtil.maxLength(request.getHeader("User-Agent"), MAX_USER_AGENT_LENGTH);
    }

    private String currentIp() {
        HttpServletRequest request = currentRequest();
        return request == null ? "" : JakartaServletUtil.getClientIP(request);
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
