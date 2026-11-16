package top.mddata.workbench.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialParameters;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.BizException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.util.ContextUtil;
import top.mddata.common.entity.User;
import top.mddata.workbench.config.FingerprintProperties;
import top.mddata.workbench.dto.FingerprintLoginVerifyDto;
import top.mddata.workbench.dto.FingerprintRegisterVerifyDto;
import top.mddata.workbench.dto.FingerprintRenameDto;
import top.mddata.workbench.entity.FingerprintCredential;
import top.mddata.workbench.handler.LoginStrategy;
import top.mddata.workbench.handler.impl.UsernameLoginStrategyImpl;
import top.mddata.workbench.mapper.FingerprintCredentialMapper;
import top.mddata.workbench.service.LoginSessionService;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.util.FingerprintUserHandles;
import top.mddata.workbench.vo.FingerprintRegisterOptionsVo;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 指纹登录服务绑定侧行为单测。
 *
 * <p>覆盖录入前密码校验（票据发放/失败路径/未登录/用户不存在）、录入挑战获取
 * （票据一次性消费、挑战生成）、录入结果校验（挑战过期、数量上限、响应非法）。</p>
 *
 * <p>登录侧覆盖挑战过期、认证结果非成功、签名计数器校验失败三条拒绝路径。
 * loginVerify 成功路径与用户禁用路径依赖真实浏览器产出的
 * AssertionRequest/认证响应 JSON，纯单测构造成本过高，由验收阶段手工联调覆盖。</p>
 *
 * @author tangyh
 * @since 2026/11/13
 */
class FingerprintServiceImplTest {

    private FingerprintCredentialMapper mapper;
    private CacheOps cacheOps;
    private RelyingParty relyingParty;
    private FingerprintProperties properties;
    private SsoUserService ssoUserService;
    private LoginSessionService loginSessionService;
    private Map<String, LoginStrategy> loginStrategy;
    private FingerprintServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(FingerprintCredentialMapper.class);
        cacheOps = mock(CacheOps.class);
        relyingParty = mock(RelyingParty.class);
        properties = new FingerprintProperties();
        // verifyRegistration 重建请求时 RelyingPartyIdentity 需要非空 rpId
        properties.setRpId("localhost");
        // 除开关校验用例外，其余用例默认指纹登录已启用
        properties.setEnabled(true);
        ssoUserService = mock(SsoUserService.class);
        loginSessionService = mock(LoginSessionService.class);
        loginStrategy = mock(Map.class);
        when(loginStrategy.get("USERNAME")).thenReturn(mock(UsernameLoginStrategyImpl.class));
        service = new FingerprintServiceImpl(relyingParty, properties, ssoUserService,
                loginSessionService, loginStrategy, mapper, cacheOps);
    }

    @Test
    @DisplayName("未登录时校验密码被拒绝")
    void 未登录时校验密码被拒绝() {
        try (MockedStatic<ContextUtil> mocked = mockStatic(ContextUtil.class)) {
            assertThrows(BizException.class, () -> service.checkPassword("pwd"));
        }
    }

    @Test
    @DisplayName("用户不存在时校验密码被拒绝")
    void 用户不存在时校验密码被拒绝() {
        try (MockedStatic<ContextUtil> mocked = mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(1L);
            when(ssoUserService.getById(1L)).thenReturn(null);

            assertThrows(BizException.class, () -> service.checkPassword("pwd"));
        }
        verify(cacheOps, never()).set(any(), anyString());
    }

    @Test
    @DisplayName("密码校验通过后发放一次性票据")
    void 密码校验通过后发放一次性票据() {
        try (MockedStatic<ContextUtil> mocked = mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(1L);
            User user = user();
            when(ssoUserService.getById(1L)).thenReturn(user);

            assertTrue(service.checkPassword("pwd").getData());

            verify(cacheOps).set(any(), eq("1"));
        }
    }

    @Test
    @DisplayName("密码校验失败时不发放票据")
    void 密码校验失败时不发放票据() {
        try (MockedStatic<ContextUtil> mocked = mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(1L);
            User user = user();
            when(ssoUserService.getById(1L)).thenReturn(user);
            LoginStrategy usernameStrategy = loginStrategy.get("USERNAME");
            doThrow(new BizException("用户名或密码错误"))
                    .when(usernameStrategy).checkUserPassword(any(), any());

            assertThrows(BizException.class, () -> service.checkPassword("wrong"));

            verify(cacheOps, never()).set(any(), anyString());
        }
    }

    @Test
    @DisplayName("登录策略未配置时拒绝")
    void 登录策略未配置时拒绝() {
        when(loginStrategy.get("USERNAME")).thenReturn(null);
        try (MockedStatic<ContextUtil> mocked = mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(1L);
            when(ssoUserService.getById(1L)).thenReturn(user());

            assertThrows(BizException.class, () -> service.checkPassword("pwd"));
        }
    }

    @Test
    @DisplayName("enabled=false 时获取录入挑战被拒绝")
    void enabled关闭时获取录入挑战被拒绝() {
        properties.setEnabled(false);
        try (MockedStatic<ContextUtil> mocked = mockStatic(ContextUtil.class)) {
            BizException ex = assertThrows(BizException.class, () -> service.registrationOptions());
            assertEquals("指纹登录未启用", ex.getMessage());
        }
        // 开关关闭时不生成挑战
        verify(relyingParty, never()).startRegistration(any(StartRegistrationOptions.class));
    }

    @Test
    @DisplayName("未验证密码直接获取录入挑战被拒绝")
    void 未验证密码直接获取录入挑战被拒绝() {
        try (MockedStatic<ContextUtil> mocked = mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(1L);
            when(cacheOps.get(any(CacheKey.class))).thenReturn(null);

            BizException ex = assertThrows(BizException.class, () -> service.registrationOptions());
            assertEquals("请先验证登录密码", ex.getMessage());
        }
    }

    @Test
    @DisplayName("验证密码后获取录入挑战成功返回挑战ID和选项JSON")
    void 验证密码后获取录入挑战成功返回挑战ID和选项JSON() {
        try (MockedStatic<ContextUtil> mocked = mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(1L);
            when(cacheOps.get(any(CacheKey.class)))
                    .thenReturn(new top.mddata.base.cache.redis.CacheResult<>("pwd-key", "1"));
            when(ssoUserService.getById(1L)).thenReturn(user());
            when(relyingParty.startRegistration(any(StartRegistrationOptions.class)))
                    .thenReturn(creationOptions());

            FingerprintRegisterOptionsVo vo = service.registrationOptions();

            assertNotNull(vo.getChallengeId());
            assertTrue(vo.getOptionsJson().contains("challenge"));
            // 密码票据一次性消费
            verify(cacheOps).del(any(CacheKey.class));
            // 挑战已缓存供 verifyRegistration 消费
            verify(cacheOps).set(any(), anyString());
        }
    }

    @Test
    @DisplayName("录入挑战过期时验证被拒绝")
    void 录入挑战过期时验证被拒绝() {
        try (MockedStatic<ContextUtil> mocked = mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(1L);
            when(cacheOps.get(any(CacheKey.class))).thenReturn(null);

            BizException ex = assertThrows(BizException.class,
                    () -> service.verifyRegistration(verifyDto()));
            assertEquals("录入请求已过期，请重新录入", ex.getMessage());
        }
    }

    @Test
    @DisplayName("凭证数量达到上限时录入被拒绝")
    void 凭证数量达到上限时录入被拒绝() {
        try (MockedStatic<ContextUtil> mocked = mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(1L);
            when(cacheOps.get(any(CacheKey.class)))
                    .thenReturn(new top.mddata.base.cache.redis.CacheResult<>(
                            "challenge-key", "Zm9vYmFy"));
            when(mapper.selectCountByQuery(any())).thenReturn(10L);

            BizException ex = assertThrows(BizException.class,
                    () -> service.verifyRegistration(verifyDto()));
            assertEquals("指纹数量已达上限，请先删除部分设备", ex.getMessage());
            // 上限校验在用户查询之前，失败时不查用户
            verify(ssoUserService, never()).getById(anyLong());
        }
    }

    @Test
    @DisplayName("录入响应非法时验证失败")
    void 录入响应非法时验证失败() {
        try (MockedStatic<ContextUtil> mocked = mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(1L);
            when(cacheOps.get(any(CacheKey.class)))
                    .thenReturn(new top.mddata.base.cache.redis.CacheResult<>(
                            "challenge-key", "Zm9vYmFy"));
            when(mapper.selectCountByQuery(any())).thenReturn(0L);
            when(ssoUserService.getById(1L)).thenReturn(user());

            BizException ex = assertThrows(BizException.class,
                    () -> service.verifyRegistration(verifyDto()));
            assertEquals("指纹录入失败：该设备可能已录入过指纹，或验证未通过，请重试", ex.getMessage());
        }
    }

    @Test
    @DisplayName("删除他人指纹凭证被拒绝")
    void 删除他人指纹凭证被拒绝() {
        try (var mocked = mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(2L);
            FingerprintCredential record = new FingerprintCredential();
            // setId 定义在 BaseEntity，链式返回 BaseEntity，需单独调用
            record.setId(100L);
            record.setUserId(1L);
            when(mapper.selectOneById(100L)).thenReturn(record);
            BizException ex = assertThrows(BizException.class, () -> service.delete(100L));
            assertEquals("无权操作他人指纹凭证", ex.getMessage());
        }
    }

    @Test
    @DisplayName("删除自己的指纹凭证走逻辑删除")
    void 删除自己的指纹凭证走逻辑删除() {
        try (var mocked = mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(1L);
            FingerprintCredential record = new FingerprintCredential();
            record.setId(100L);
            record.setUserId(1L);
            when(mapper.selectOneById(100L)).thenReturn(record);
            when(mapper.deleteById(100L)).thenReturn(1);
            Boolean result = service.delete(100L);
            assertEquals(Boolean.TRUE, result);
        }
    }

    @Test
    @DisplayName("改名校验归属后更新设备名")
    void 改名校验归属后更新设备名() {
        try (var mocked = mockStatic(ContextUtil.class)) {
            mocked.when(ContextUtil::getUserId).thenReturn(1L);
            FingerprintCredential record = new FingerprintCredential();
            record.setId(100L);
            record.setUserId(1L);
            record.setDeviceName("旧名");
            when(mapper.selectOneById(100L)).thenReturn(record);
            // SuperServiceImpl.updateById 底层调用 mapper.update(entity, true)
            when(mapper.update(any(FingerprintCredential.class), anyBoolean())).thenReturn(1);
            Boolean result = service.rename(100L,
                    new FingerprintRenameDto().setId(100L).setName("新名"));
            assertEquals(Boolean.TRUE, result);
            assertEquals("新名", record.getDeviceName());
        }
    }

    @Test
    @DisplayName("enabled=false 时获取登录挑战被拒绝")
    void enabled关闭时获取登录挑战被拒绝() {
        properties.setEnabled(false);
        BizException ex = assertThrows(BizException.class, () -> service.loginOptions());
        assertEquals("指纹登录未启用", ex.getMessage());
        // 开关关闭时不生成挑战
        verify(relyingParty, never()).startAssertion(any());
    }

    @Test
    @DisplayName("登录挑战过期时验证被拒绝")
    void 登录挑战过期时验证被拒绝() {
        FingerprintLoginVerifyDto dto = new FingerprintLoginVerifyDto();
        dto.setChallengeId("gone");
        when(cacheOps.get(any(CacheKey.class))).thenReturn(null);
        BizException ex = assertThrows(BizException.class, () -> service.loginVerify(dto));
        assertEquals("登录请求已过期，请重新验证", ex.getMessage());
    }

    @Test
    @DisplayName("认证结果非成功时抛出指纹验证失败")
    void 认证结果非成功时抛出指纹验证失败() throws Exception {
        when(cacheOps.get(any(CacheKey.class)))
                .thenReturn(new top.mddata.base.cache.redis.CacheResult<>(
                        "challenge-key", loginAssertionRequest().toJson()));
        AssertionResult result = mock(AssertionResult.class);
        when(result.isSuccess()).thenReturn(false);
        when(relyingParty.finishAssertion(any(FinishAssertionOptions.class))).thenReturn(result);

        try (MockedStatic<PublicKeyCredential> pkc = mockStatic(PublicKeyCredential.class)) {
            @SuppressWarnings("unchecked")
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>
                    response = mock(PublicKeyCredential.class);
            pkc.when(() -> PublicKeyCredential.parseAssertionResponseJson(anyString()))
                    .thenReturn(response);

            BizException ex = assertThrows(BizException.class,
                    () -> service.loginVerify(loginVerifyDto()));
            assertEquals("指纹验证失败", ex.getMessage());
        }
        verify(loginSessionService, never())
                .loginAuthenticated(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("签名计数器校验失败时抛出指纹验证失败")
    void 签名计数器校验失败时抛出指纹验证失败() throws Exception {
        when(cacheOps.get(any(CacheKey.class)))
                .thenReturn(new top.mddata.base.cache.redis.CacheResult<>(
                        "challenge-key", loginAssertionRequest().toJson()));
        AssertionResult result = mock(AssertionResult.class);
        when(result.isSuccess()).thenReturn(true);
        when(result.isSignatureCounterValid()).thenReturn(false);
        when(relyingParty.finishAssertion(any(FinishAssertionOptions.class))).thenReturn(result);

        try (MockedStatic<PublicKeyCredential> pkc = mockStatic(PublicKeyCredential.class)) {
            @SuppressWarnings("unchecked")
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>
                    response = mock(PublicKeyCredential.class);
            pkc.when(() -> PublicKeyCredential.parseAssertionResponseJson(anyString()))
                    .thenReturn(response);

            BizException ex = assertThrows(BizException.class,
                    () -> service.loginVerify(loginVerifyDto()));
            assertEquals("指纹验证失败", ex.getMessage());
        }
        verify(loginSessionService, never())
                .loginAuthenticated(any(), any(), any(), any(), any(), any());
    }

    private AssertionRequest loginAssertionRequest() {
        return AssertionRequest.builder()
                .publicKeyCredentialRequestOptions(PublicKeyCredentialRequestOptions.builder()
                        .challenge(new ByteArray(new byte[32]))
                        .rpId("localhost")
                        .userVerification(UserVerificationRequirement.REQUIRED)
                        .build())
                .build();
    }

    private FingerprintLoginVerifyDto loginVerifyDto() {
        FingerprintLoginVerifyDto dto = new FingerprintLoginVerifyDto();
        dto.setChallengeId("challenge-id")
                .setResponseJson("{}");
        return dto;
    }

    private User user() {
        User user = new User();
        // setId 定义在 BaseEntity，链式返回 BaseEntity，需单独调用
        user.setId(1L);
        user.setUsername("admin")
                .setName("管理员")
                .setSalt("salt")
                .setPassword(SecureUtil.sha256("pwd" + "salt"));
        return user;
    }

    private PublicKeyCredentialCreationOptions creationOptions() {
        return PublicKeyCredentialCreationOptions.builder()
                .rp(RelyingPartyIdentity.builder().id("localhost").name("MDP主数据平台").build())
                .user(UserIdentity.builder()
                        .name("admin")
                        .displayName("管理员")
                        .id(FingerprintUserHandles.userHandleOf(1L))
                        .build())
                .challenge(new ByteArray(new byte[32]))
                .pubKeyCredParams(java.util.List.of(PublicKeyCredentialParameters.ES256))
                .build();
    }

    private FingerprintRegisterVerifyDto verifyDto() {
        FingerprintRegisterVerifyDto dto = new FingerprintRegisterVerifyDto();
        dto.setChallengeId("challenge-id")
                .setResponseJson("{}")
                .setDeviceId("device-id")
                .setDeviceName("我的电脑")
                .setPlatform("darwin");
        return dto;
    }
}
