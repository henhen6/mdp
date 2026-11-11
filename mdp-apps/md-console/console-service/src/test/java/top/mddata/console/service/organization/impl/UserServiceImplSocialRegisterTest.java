package top.mddata.console.service.organization.impl;

import com.baidu.fsg.uid.UidGenerator;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.base.exception.BizException;
import top.mddata.common.entity.User;
import top.mddata.common.entity.UserSocialAuth;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.organization.UserSourceEnum;
import top.mddata.common.mapper.UserMapper;
import top.mddata.common.mapper.UserSocialAuthMapper;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.service.coderule.CodeRuleService;
import top.mddata.console.service.organization.OrgVisibilityService;
import top.mddata.console.service.organization.UserOrgRelService;
import top.mddata.console.service.organization.UserService;
import top.mddata.console.service.organization.accountop.AccountOperationGuard;
import top.mddata.console.service.permission.RoleService;
import top.mddata.console.service.system.ConfigService;
import top.mddata.console.service.system.FileService;
import top.mddata.open.facade.admin.NotifyAndEventPushFacade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link UserServiceImpl#registerBySocial} 本地事务注册行为单测。
 *
 * <p>覆盖用户初始化、默认组织角色、人员编号、绑定写入与用户新增事件的顺序，
 * 绑定失败时不发布事件，以及不支持身份、用户名冲突的快速失败；同时守护普通
 * 用户名注册行为不回归。</p>
 *
 * @author tangyh
 */
class UserServiceImplSocialRegisterTest {

    private static final Long NEW_USER_ID = 123L;
    private static final String RAW_PASSWORD = "随机密码明文";

    private UserMapper userMapper;
    private UserSocialAuthMapper userSocialAuthMapper;
    private UserOrgRelService userOrgRelService;
    private RoleService roleService;
    private CodeRuleService codeRuleService;
    private NotifyAndEventPushFacade notifyFacade;
    private UserService service;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        // IService.save 调用 insert(entity, ignoreNulls)，模拟 MyBatis-Flex 回填主键
        when(userMapper.insert(any(User.class), anyBoolean())).thenAnswer(invocation -> {
            invocation.getArgument(0, User.class).setId(NEW_USER_ID);
            return 1;
        });
        when(userMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);

        userSocialAuthMapper = mock(UserSocialAuthMapper.class);
        when(userSocialAuthMapper.insert(any(UserSocialAuth.class))).thenReturn(1);

        userOrgRelService = mock(UserOrgRelService.class);
        roleService = mock(RoleService.class);
        codeRuleService = mock(CodeRuleService.class);
        when(codeRuleService.generate(anyString(), any())).thenReturn("USER_NO_1");
        notifyFacade = mock(NotifyAndEventPushFacade.class);

        ConfigService configService = mock(ConfigService.class);
        when(configService.getString(any(), any())).thenReturn("3M");
        SystemProperties systemProperties = mock(SystemProperties.class);
        when(systemProperties.getDefPwd()).thenReturn("123456");
        UidGenerator uidGenerator = mock(UidGenerator.class);
        when(uidGenerator.getUid()).thenReturn(NEW_USER_ID);

        service = new UserServiceImpl(userOrgRelService, mock(FileService.class),
                configService, systemProperties, uidGenerator, notifyFacade, roleService,
                mock(OrgVisibilityService.class), mock(AccountOperationGuard.class),
                codeRuleService, userSocialAuthMapper);
        ReflectionTestUtils.setField(service, "mapper", userMapper);
    }

    @Test
    void 社交注册按顺序创建用户写入绑定后发布事件() {
        User user = socialUser();
        UserSocialAuth socialAuth = socialAuth();

        boolean result = service.registerBySocial(user, socialAuth);

        assertTrue(result);
        assertEquals(NEW_USER_ID, socialAuth.getUserId());
        assertNotNull(socialAuth.getBoundAt());
        assertEquals(NEW_USER_ID, user.getId());
        assertEquals(UserSourceEnum.PLATFORM.getCode(), user.getUserSource());
        assertNotNull(user.getSalt());
        assertNotEquals(RAW_PASSWORD, user.getPassword());
        assertEquals("USER_NO_1", user.getUserNo());
        assertEquals("测试用户", user.getName());

        InOrder order = inOrder(userMapper, userSocialAuthMapper, notifyFacade);
        order.verify(userMapper).insert(user, true);
        order.verify(userSocialAuthMapper).insert(socialAuth);
        order.verify(notifyFacade).eventPush(any());
        verify(userOrgRelService).save(any());
        verify(roleService).joinTheRole(anyString(), eq(NEW_USER_ID));
    }

    @Test
    void 绑定插入异常时不发布用户新增事件() {
        doThrow(new RuntimeException("绑定写入失败"))
                .when(userSocialAuthMapper).insert(any(UserSocialAuth.class));

        assertThrows(Exception.class,
                () -> service.registerBySocial(socialUser(), socialAuth()));

        verify(userMapper).insert(any(User.class), anyBoolean());
        verify(notifyFacade, never()).eventPush(any());
    }

    @Test
    void 绑定唯一键冲突时转译可理解业务异常且不发布事件() {
        doThrow(new DuplicateKeyException("Duplicate entry"))
                .when(userSocialAuthMapper).insert(any(UserSocialAuth.class));

        BizException ex = assertThrows(BizException.class,
                () -> service.registerBySocial(socialUser(), socialAuth()));

        assertTrue(ex.getMessage().contains("第三方账号已被其他用户绑定"));
        verify(notifyFacade, never()).eventPush(any());
    }

    @Test
    void 不支持的注册身份快速失败() {
        User user = socialUser();
        user.setNature(99);

        assertThrows(Exception.class, () -> service.registerBySocial(user, socialAuth()));

        verify(userMapper, never()).insert(any(User.class), anyBoolean());
        verifyNoInteractions(userSocialAuthMapper, notifyFacade);
    }

    @Test
    void 用户名已存在时快速失败() {
        when(userMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);

        assertThrows(Exception.class,
                () -> service.registerBySocial(socialUser(), socialAuth()));

        verify(userMapper, never()).insert(any(User.class), anyBoolean());
        verifyNoInteractions(userSocialAuthMapper, notifyFacade);
    }

    @Test
    void 普通用户名注册行为不回归() {
        User user = new User();
        user.setUsername("zhangsan");
        user.setPassword("123456");
        user.setNature(OrgNatureEnum.HEAD_COMPANY.getCode());

        boolean result = service.registerByUsername(user);

        assertTrue(result);
        assertEquals("zhangsan", user.getName());
        assertEquals(NEW_USER_ID, user.getId());
        verify(userMapper).insert(user, true);
        verify(userOrgRelService).save(any());
        verify(roleService).joinTheRole(anyString(), eq(NEW_USER_ID));
        verify(notifyFacade).eventPush(any());
        verifyNoInteractions(userSocialAuthMapper);
    }

    private User socialUser() {
        User user = new User();
        user.setUsername("social_ticket_user");
        user.setPassword(RAW_PASSWORD);
        user.setName("测试用户");
        user.setNature(OrgNatureEnum.HEAD_COMPANY.getCode());
        return user;
    }

    private UserSocialAuth socialAuth() {
        UserSocialAuth socialAuth = new UserSocialAuth();
        socialAuth.setProvider("QQ");
        socialAuth.setClientId("client-id");
        socialAuth.setOpenId("open-id");
        socialAuth.setNickname("测试用户");
        return socialAuth;
    }
}
