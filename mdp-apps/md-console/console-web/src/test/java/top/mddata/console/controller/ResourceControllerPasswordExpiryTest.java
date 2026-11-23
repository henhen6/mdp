package top.mddata.console.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import top.mddata.base.base.R;
import top.mddata.base.fieldperm.spi.FieldPermProvider;
import top.mddata.common.constant.ConfigKey;
import top.mddata.common.entity.User;
import top.mddata.common.properties.IgnoreProperties;
import top.mddata.console.service.organization.UserService;
import top.mddata.console.service.permission.ResourceMenuService;
import top.mddata.console.service.permission.RoleService;
import top.mddata.console.service.system.ConfigService;
import top.mddata.console.vo.permission.UserinfoVo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 用户信息接口密码过期状态组装的控制器测试。
 *
 * <p>覆盖：临期用户返回 EXPIRING 与剩余天数；配置读取异常时降级关闭提醒且不向外抛出；
 * 配置值非法（null 或负数）时降级为 0。</p>
 */
@ExtendWith(MockitoExtension.class)
class ResourceControllerPasswordExpiryTest {

    private static final long USER_ID = 1L;

    @Mock
    private ResourceMenuService resourceMenuService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserService userService;

    @Mock
    private ConfigService configService;

    @Mock
    private IgnoreProperties ignoreProperties;

    @Mock
    private FieldPermProvider fieldPermProvider;

    @InjectMocks
    private ResourceController resourceController;

    @Test
    void getUserinfo_密码3天后过期且阈值7天_返回临期状态() {
        LocalDate expireDate = LocalDate.now().plusDays(3);
        User user = buildUser(expireDate.atTime(12, 0));
        when(userService.getByIdCache(USER_ID)).thenReturn(user);
        when(configService.getInteger(
                ConfigKey.Workbench.PASSWORD_EXPIRE_REMIND_DAYS, 7)).thenReturn(7);

        R<UserinfoVo> response;
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            response = resourceController.getUserinfo();
        }

        // 期望剩余天数以测试执行后的日期计算，避免跨午夜断言脆弱
        long expectedRemainDays = ChronoUnit.DAYS.between(LocalDate.now(), expireDate);
        UserinfoVo data = response.getData();
        assertNotNull(data);
        assertEquals("EXPIRING", data.getPasswordExpireStatus());
        assertEquals(expectedRemainDays, data.getPasswordExpireRemainDays());
        assertEquals(7, data.getPasswordExpireRemindDays());
    }

    @Test
    void getUserinfo_用户不存在_返回空信息且不组装密码状态() {
        when(userService.getByIdCache(USER_ID)).thenReturn(null);

        R<UserinfoVo> response;
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            response = assertDoesNotThrow(() -> resourceController.getUserinfo());
        }

        UserinfoVo data = response.getData();
        assertNotNull(data);
        assertNull(data.getPasswordExpireStatus());
        assertNull(data.getPasswordExpireRemainDays());
        assertNull(data.getPasswordExpireRemindDays());
        verifyNoInteractions(configService);
    }

    @Test
    void getUserinfo_读取提醒配置抛异常_降级为0且不向外抛出() {
        LocalDate expireDate = LocalDate.now().plusDays(3);
        User user = buildUser(expireDate.atTime(12, 0));
        when(userService.getByIdCache(USER_ID)).thenReturn(user);
        when(configService.getInteger(anyString(), anyInt()))
                .thenThrow(new RuntimeException("配置服务不可用"));

        R<UserinfoVo> response;
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            response = assertDoesNotThrow(() -> resourceController.getUserinfo());
        }

        long expectedRemainDays = ChronoUnit.DAYS.between(LocalDate.now(), expireDate);
        UserinfoVo data = response.getData();
        assertNotNull(data);
        assertEquals("admin", data.getUsername());
        assertEquals(0, data.getPasswordExpireRemindDays());
        assertEquals("NORMAL", data.getPasswordExpireStatus());
        assertEquals(expectedRemainDays, data.getPasswordExpireRemainDays());
    }

    @ParameterizedTest(name = "提醒天数配置非法值 {0} 时降级为0")
    @MethodSource("invalidRemindDaysProvider")
    void getUserinfo_提醒天数非法_降级为0(Integer invalidDays) {
        User user = buildUser(LocalDateTime.now().plusDays(3));
        when(userService.getByIdCache(USER_ID)).thenReturn(user);
        when(configService.getInteger(
                ConfigKey.Workbench.PASSWORD_EXPIRE_REMIND_DAYS, 7)).thenReturn(invalidDays);

        R<UserinfoVo> response;
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getLoginIdAsLong).thenReturn(USER_ID);
            response = resourceController.getUserinfo();
        }

        assertEquals(0, response.getData().getPasswordExpireRemindDays());
        assertEquals("NORMAL", response.getData().getPasswordExpireStatus());
    }

    static Stream<Arguments> invalidRemindDaysProvider() {
        return Stream.of(Arguments.of((Integer) null), Arguments.of(-1));
    }

    private User buildUser(LocalDateTime pwExpireTime) {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername("admin");
        user.setPwExpireTime(pwExpireTime);
        return user;
    }
}
