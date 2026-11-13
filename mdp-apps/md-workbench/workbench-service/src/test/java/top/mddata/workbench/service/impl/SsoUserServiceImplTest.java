package top.mddata.workbench.service.impl;

import cn.hutool.crypto.SecureUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.ArgumentException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.util.ContextUtil;
import top.mddata.common.cache.workbench.SsoUserEmailCacheKeyBuilder;
import top.mddata.common.cache.workbench.SsoUserPhoneCacheKeyBuilder;
import top.mddata.common.cache.workbench.SsoUserUserNameCacheKeyBuilder;
import top.mddata.common.constant.MsgTemplateKey;
import top.mddata.common.entity.User;
import top.mddata.common.mapper.OrgMapper;
import top.mddata.console.facade.system.ConfigFacade;
import top.mddata.console.facade.system.FileFacade;
import top.mddata.open.facade.admin.NotifyAndEventPushFacade;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.workbench.dto.ProfileEmailDto;
import top.mddata.workbench.dto.ProfilePasswordDto;
import top.mddata.workbench.dto.ProfilePhoneDto;
import top.mddata.workbench.dto.ProfileUsernameDto;
import top.mddata.workbench.service.VerificationCodeService;

import java.lang.reflect.Field;
import top.mddata.common.cache.workbench.CaptchaCacheKeyBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SsoUserServiceImpl} 修改用户名（登录账号）单测。
 *
 * <p>核心约束：先校验当前密码、再校验用户名唯一，二者都通过才更新；
 * 更新后需同时清理「旧用户名」「新用户名」的查询缓存，避免旧映射残留。</p>
 *
 * <p>另覆盖手机号/邮箱换绑：首次绑定（原值为空）时放行，已有原值时才校验一致。</p>
 *
 * @author tangyh
 */
class SsoUserServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final String SALT = "unit-test-salt";
    private static final String PASSWORD = "correct-password";

    private CacheOps cacheOps;
    private ConfigFacade configFacade;
    private VerificationCodeService verificationCodeService;
    private SsoUserServiceImpl service;

    private MockedStatic<ContextUtil> contextUtilMock;
    private MockedStatic<SsoUserUserNameCacheKeyBuilder> userNameKeyBuilderMock;

    @BeforeEach
    void setUp() {
        cacheOps = mock(CacheOps.class);
        configFacade = mock(ConfigFacade.class);
        verificationCodeService = mock(VerificationCodeService.class);
        service = spy(new SsoUserServiceImpl(
                mock(OrgMapper.class),
                mock(FileFacade.class),
                cacheOps,
                configFacade,
                verificationCodeService,
                mock(NotifyAndEventPushFacade.class)));
        injectSuperCacheOps(cacheOps);

        contextUtilMock = mockStatic(ContextUtil.class);
        contextUtilMock.when(() -> ContextUtil.getUserId()).thenReturn(USER_ID);
        userNameKeyBuilderMock = mockStatic(SsoUserUserNameCacheKeyBuilder.class);
        userNameKeyBuilderMock
                .when(() -> SsoUserUserNameCacheKeyBuilder.builder(anyString()))
                .thenAnswer(inv -> mock(CacheKey.class));
    }

    @AfterEach
    void tearDown() {
        contextUtilMock.close();
        userNameKeyBuilderMock.close();
    }

    private User currentUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername("old-username");
        user.setSalt(SALT);
        user.setPassword(SecureUtil.sha256(PASSWORD + SALT));
        return user;
    }

    /**
     * 父类 SuperServiceImpl 的 cacheOps 是 @Autowired 字段注入，单测中需手动注入供 delCache/getByIdCache 使用。
     */
    private void injectSuperCacheOps(CacheOps ops) {
        try {
            Field field = SuperServiceImpl.class.getDeclaredField("cacheOps");
            field.setAccessible(true);
            field.set(service, ops);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("注入父类 cacheOps 失败", ex);
        }
    }

    private ProfileUsernameDto dto(String username, String password) {
        ProfileUsernameDto dto = new ProfileUsernameDto();
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }

    @Test
    void 当前密码错误时拒绝修改() {
        doReturn(currentUser()).when(service).getById(USER_ID);

        assertThrows(ArgumentException.class,
                () -> service.updateUsername(dto("new-username", "wrong-password")));

        verify(service, never()).updateById(any(User.class));
        verify(cacheOps, never()).del(anyCollection());
    }

    @Test
    void 用户名已被占用时拒绝修改() {
        doReturn(currentUser()).when(service).getById(USER_ID);
        doReturn(true).when(service).checkUsername(anyString(), any());

        assertThrows(ArgumentException.class,
                () -> service.updateUsername(dto("taken-username", PASSWORD)));

        verify(service, never()).updateById(any(User.class));
    }

    @Test
    void 密码正确且用户名唯一时更新用户名并清理新旧用户名缓存() {
        doReturn(currentUser()).when(service).getById(USER_ID);
        doReturn(false).when(service).checkUsername(anyString(), any());
        doReturn(true).when(service).updateById(any(User.class));

        Long id = service.updateUsername(dto("new-username", PASSWORD));

        assertEquals(USER_ID, id);
        // 修改用户名后必须淘汰 id 实体缓存，否则 getByIdCache 返回脏数据
        verify(cacheOps).del(any(CacheKey[].class));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(service).updateById(userCaptor.capture());
        assertEquals("new-username", userCaptor.getValue().getUsername());

        verify(cacheOps).del(anyCollection());
        userNameKeyBuilderMock.verify(
                () -> SsoUserUserNameCacheKeyBuilder.builder("old-username"));
        userNameKeyBuilderMock.verify(
                () -> SsoUserUserNameCacheKeyBuilder.builder("new-username"));
    }

    @Test
    void 无原手机号时允许首次绑定并发送验证码() {
        User user = currentUser();
        doReturn(user).when(service).getById(USER_ID);
        doReturn(false).when(service).checkPhone(anyString(), any());

        // 前端对无原手机号的用户传空串
        service.sendPhoneCode("", "13800000000");

        verify(verificationCodeService).sendPhoneCode("13800000000", MsgTemplateKey.Sms.PHONE_EDIT);
    }

    @Test
    void 无原邮箱时允许首次绑定并发送验证码() {
        User user = currentUser();
        doReturn(user).when(service).getById(USER_ID);
        doReturn(false).when(service).checkEmail(anyString(), any());

        service.sendEmailCode("", "user@example.com");

        verify(verificationCodeService).sendEmailCode("user@example.com", MsgTemplateKey.Email.EMAIL_EDIT);
    }

    @Test
    void 原手机号一致时正常发送验证码() {
        User user = currentUser();
        user.setPhone("13800000001");
        doReturn(user).when(service).getById(USER_ID);
        doReturn(false).when(service).checkPhone(anyString(), any());

        service.sendPhoneCode("13800000001", "13800000000");

        verify(verificationCodeService).sendPhoneCode("13800000000", MsgTemplateKey.Sms.PHONE_EDIT);
    }

    @Test
    void 原手机号非空但不一致时拒绝发送验证码() {
        User user = currentUser();
        user.setPhone("13800000001");
        doReturn(user).when(service).getById(USER_ID);

        assertThrows(ArgumentException.class,
                () -> service.sendPhoneCode("13899999999", "13800000000"));

        verify(verificationCodeService, never()).sendPhoneCode(anyString(), anyString());
    }

    @Test
    void 无原手机号时允许换绑且不清理空缓存键() {
        User user = currentUser();
        doReturn(user).when(service).getById(USER_ID);
        doReturn(false).when(service).checkPhone(anyString(), any());
        doReturn(true).when(service).updateById(any(User.class));

        ProfilePhoneDto dto = new ProfilePhoneDto();
        dto.setOldPhone("");
        dto.setPhone("13800000000");
        dto.setKey("captcha-key");
        dto.setCode("1234");

        // CacheResult 构造函数会调用 CacheKey 的方法，必须在任何 stubbing 之外提前构造
        CacheKey captchaKey = mock(CacheKey.class);
        CacheKey newPhoneKey = mock(CacheKey.class);
        CacheResult<String> captchaResult = new CacheResult<>(captchaKey, "1234");
        try (MockedStatic<CaptchaCacheKeyBuilder> captchaMock = mockStatic(CaptchaCacheKeyBuilder.class);
             MockedStatic<SsoUserPhoneCacheKeyBuilder> phoneKeyMock = mockStatic(SsoUserPhoneCacheKeyBuilder.class)) {
            captchaMock.when(() -> CaptchaCacheKeyBuilder.build(anyString(), anyString()))
                    .thenReturn(captchaKey);
            phoneKeyMock.when(() -> SsoUserPhoneCacheKeyBuilder.builder(isNull()))
                    .thenReturn(newPhoneKey);
            phoneKeyMock.when(() -> SsoUserPhoneCacheKeyBuilder.builder(anyString()))
                    .thenReturn(newPhoneKey);
            doReturn(captchaResult).when(cacheOps).get(any(CacheKey.class));

            Long id = service.updatePhone(dto);

            assertEquals(USER_ID, id);
            // 修改手机号后必须淘汰 id 实体缓存，否则 getByIdCache 返回脏数据
            verify(cacheOps).del(any(CacheKey[].class));
            // 只清理新手机号的缓存键，禁止以 null/空串构建旧缓存键
            phoneKeyMock.verify(() -> SsoUserPhoneCacheKeyBuilder.builder("13800000000"));
            phoneKeyMock.verify(() -> SsoUserPhoneCacheKeyBuilder.builder(isNull()), never());
            phoneKeyMock.verify(() -> SsoUserPhoneCacheKeyBuilder.builder(""), never());
        }
    }

    @Test
    void 原手机号不一致时拒绝换绑() {
        User user = currentUser();
        user.setPhone("13800000001");
        doReturn(user).when(service).getById(USER_ID);

        ProfilePhoneDto dto = new ProfilePhoneDto();
        dto.setOldPhone("13899999999");
        dto.setPhone("13800000000");
        dto.setKey("captcha-key");
        dto.setCode("1234");

        assertThrows(ArgumentException.class, () -> service.updatePhone(dto));

        verify(service, never()).updateById(any(User.class));
        verify(cacheOps, never()).del(anyCollection());
    }

    @Test
    void 微信换绑手机号后淘汰用户id缓存() {
        User user = currentUser();
        doReturn(user).when(service).getById(USER_ID);
        doReturn(false).when(service).checkPhone(anyString(), any());
        doReturn(true).when(service).updateById(any(User.class));

        try (MockedStatic<SsoUserPhoneCacheKeyBuilder> phoneKeyMock = mockStatic(SsoUserPhoneCacheKeyBuilder.class)) {
            phoneKeyMock.when(() -> SsoUserPhoneCacheKeyBuilder.builder(anyString()))
                    .thenReturn(mock(CacheKey.class));

            Long id = service.updatePhoneByWxMa("13800000000");

            assertEquals(USER_ID, id);
            verify(cacheOps).del(any(CacheKey[].class));
        }
    }

    @Test
    void 修改邮箱后淘汰用户id缓存() {
        User user = currentUser();
        doReturn(user).when(service).getById(USER_ID);
        doReturn(false).when(service).checkEmail(anyString(), any());
        doReturn(true).when(service).updateById(any(User.class));

        // CacheResult 构造函数会调用 CacheKey 的方法，必须在任何 stubbing 之外提前构造
        CacheKey emailCaptchaKey = mock(CacheKey.class);
        CacheResult<String> captchaResult = new CacheResult<>(emailCaptchaKey, "1234");
        try (MockedStatic<CaptchaCacheKeyBuilder> captchaMock = mockStatic(CaptchaCacheKeyBuilder.class);
             MockedStatic<SsoUserEmailCacheKeyBuilder> emailKeyMock = mockStatic(SsoUserEmailCacheKeyBuilder.class)) {
            captchaMock.when(() -> CaptchaCacheKeyBuilder.build(anyString(), anyString()))
                    .thenReturn(emailCaptchaKey);
            emailKeyMock.when(() -> SsoUserEmailCacheKeyBuilder.builder(anyString()))
                    .thenReturn(mock(CacheKey.class));
            doReturn(captchaResult).when(cacheOps).get(any(CacheKey.class));

            ProfileEmailDto dto = new ProfileEmailDto();
            dto.setOldEmail("");
            dto.setEmail("user@example.com");
            dto.setKey("captcha-key");
            dto.setCode("1234");

            Long id = service.updateEmail(dto);

            assertEquals(USER_ID, id);
            verify(cacheOps).del(any(CacheKey[].class));
        }
    }

    @Test
    void 修改密码后淘汰用户id缓存() {
        doReturn(currentUser()).when(service).getById(USER_ID);
        doReturn(true).when(service).updateById(any(User.class));
        when(configFacade.getString(anyString(), anyString())).thenReturn("3M");

        ProfilePasswordDto dto = new ProfilePasswordDto();
        dto.setOldPassword(PASSWORD);
        dto.setPassword("new-password");
        dto.setConfirmPassword("new-password");

        Long id = service.updatePassword(dto);

        assertEquals(USER_ID, id);
        verify(cacheOps).del(any(CacheKey[].class));
    }

    @Test
    void 邮箱重置密码后淘汰用户id缓存() {
        doReturn(currentUser()).when(service).getByEmail("user@example.com");
        doReturn(true).when(service).updateById(any(User.class));
        when(configFacade.getString(anyString(), anyString())).thenReturn("3M");

        Boolean ok = service.resetPwByEmail("user@example.com", "new-password");

        assertTrue(ok);
        verify(cacheOps).del(any(CacheKey[].class));
    }
}