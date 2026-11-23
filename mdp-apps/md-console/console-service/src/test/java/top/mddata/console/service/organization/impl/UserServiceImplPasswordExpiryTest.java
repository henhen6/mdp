package top.mddata.console.service.organization.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.common.entity.User;
import top.mddata.common.mapper.UserMapper;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.dto.organization.UserResetPasswordDto;
import top.mddata.console.service.organization.UserOrgRelService;
import top.mddata.console.service.organization.UserRoleRelService;
import top.mddata.console.service.organization.accountop.AccountOperationGuard;
import top.mddata.console.service.system.ConfigService;
import top.mddata.console.service.system.FileService;
import top.mddata.open.facade.admin.NotifyAndEventPushFacade;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserServiceImpl 管理员重置密码的续期豁免测试（规格 §5.2.1）：
 * 旧 pwExpireTime 为 null 的用户不覆盖，有值才重算。
 */
class UserServiceImplPasswordExpiryTest {

    private static final Long USER_ID = 20L;

    private AutoCloseable mocks;
    @Mock
    private UserMapper mapper;
    @Mock
    private CacheOps cacheOps;
    @Mock
    private AccountOperationGuard accountOperationGuard;
    @Mock
    private ConfigService configService;
    @Mock
    private SystemProperties systemProperties;
    @Mock
    private NotifyAndEventPushFacade notifyAndEventPushFacade;
    @Mock
    private FileService fileService;
    @Mock
    private UserOrgRelService userOrgRelService;
    @Mock
    private UserRoleRelService userRoleRelService;
    @Mock
    private top.mddata.console.service.organization.SystemProtectService systemProtectService;
    @Mock
    private top.mddata.common.mapper.UserSocialAuthMapper userSocialAuthMapper;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(userService, "mapper", mapper);
        ReflectionTestUtils.setField(userService, "cacheOps", cacheOps);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    private UserResetPasswordDto resetDto() {
        UserResetPasswordDto data = new UserResetPasswordDto();
        data.setId(USER_ID);
        data.setPassword("new-password");
        return data;
    }

    @Test
    void resetPassword_旧值null_不覆盖过期时间() {
        User oldUser = new User();
        oldUser.setId(USER_ID);
        oldUser.setPwExpireTime(null);
        when(mapper.selectOneById(USER_ID)).thenReturn(oldUser);
        when(configService.getString(anyString(), anyString())).thenReturn("3M");
        userService.resetPassword(resetDto());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(mapper).update(captor.capture(), anyBoolean());
        assertNull(captor.getValue().getPwExpireTime());
    }

    @Test
    void resetPassword_旧值有值_重算过期时间() {
        User oldUser = new User();
        oldUser.setId(USER_ID);
        oldUser.setPwExpireTime(LocalDateTime.now().plusDays(1));
        when(mapper.selectOneById(USER_ID)).thenReturn(oldUser);
        when(configService.getString(anyString(), anyString())).thenReturn("3M");
        userService.resetPassword(resetDto());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(mapper).update(captor.capture(), anyBoolean());
        assertNotNull(captor.getValue().getPwExpireTime());
        assertTrue(captor.getValue().getPwExpireTime().isAfter(LocalDateTime.now()));
    }
}
