package top.mddata.console.service.organization.impl;

import com.baidu.fsg.uid.UidGenerator;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.common.entity.User;
import top.mddata.common.mapper.UserMapper;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.dto.organization.UserDto;
import top.mddata.console.service.organization.OrgVisibilityService;
import top.mddata.console.service.organization.UserOrgRelService;
import top.mddata.console.service.organization.accountop.AccountOperationGuard;
import top.mddata.console.service.permission.RoleService;
import top.mddata.console.service.system.ConfigService;
import top.mddata.console.service.system.FileService;
import top.mddata.open.facade.admin.NotifyAndEventPushFacade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 手机号/邮箱"允许为空、非空唯一"的归一化与校验逻辑。
 *
 * <p>背景：phone/email 列有唯一索引（uk_phone/uk_email），空串是真实值，
 * 多用户空串会撞索引；必须统一归一化为 null（MySQL 唯一索引允许多个 NULL）。</p>
 *
 * @author henhen6
 * @since 2026-10-03
 */
class UserServiceImplContactTest {

    private UserMapper userMapper;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        SystemProperties systemProperties = mock(SystemProperties.class);
        ConfigService configService = mock(ConfigService.class);
        UidGenerator uidGenerator = mock(UidGenerator.class);
        when(uidGenerator.getUid()).thenReturn(123L);
        when(configService.getString(any(), any())).thenReturn("3M");
        when(systemProperties.getDefPwd()).thenReturn("123456");
        userService = new UserServiceImpl(mock(UserOrgRelService.class), mock(FileService.class),
                configService, systemProperties, uidGenerator, mock(NotifyAndEventPushFacade.class),
                mock(RoleService.class), mock(OrgVisibilityService.class), mock(AccountOperationGuard.class));
        // mapper 由 mybatis-flex ServiceImpl 以 @Autowired 字段注入，测试中反射替换
        ReflectionTestUtils.setField(userService, "mapper", userMapper);
    }

    private UserDto dto(String phone, String email) {
        UserDto dto = new UserDto();
        dto.setUsername("zhangsan");
        dto.setPhone(phone);
        dto.setEmail(email);
        dto.setDefPassword(true);
        return dto;
    }

    @Test
    void 新增_空串与空白归一化为null() {
        when(userMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);

        User entity = userService.saveBefore(dto("", "  "));

        assertNull(entity.getPhone());
        assertNull(entity.getEmail());
    }

    @Test
    void 新增_非空值保留且通过唯一性校验() {
        when(userMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);

        User entity = userService.saveBefore(dto("13812345678", "a@b.com"));

        assertEquals("13812345678", entity.getPhone());
        assertEquals("a@b.com", entity.getEmail());
    }

    @Test
    void 新增_手机号已被占用时快速失败() {
        // 用户名不重复、手机号重复
        when(userMapper.selectCountByQuery(any(QueryWrapper.class)))
                .thenReturn(0L)
                .thenReturn(1L);

        assertThrows(Exception.class, () -> userService.saveBefore(dto("13812345678", null)));
    }

    @Test
    void check手机号_空值短路不查库() {
        assertFalse(userService.checkPhone("", null));
        assertFalse(userService.checkPhone(null, null));
        assertFalse(userService.checkEmail("  ", null));
        verifyNoInteractions(userMapper);
    }

    @Test
    void 修改_updates中的空串归一化为null() {
        User sysUser = UpdateEntity.of(User.class, 1L);
        sysUser.setPhone("");
        sysUser.setEmail("  ");

        UserServiceImpl.normalizeContactUpdates(sysUser);

        org.junit.jupiter.api.Assertions.assertTrue(
                ((com.mybatisflex.core.update.UpdateWrapper) sysUser).getUpdates().containsKey("phone"));
        assertNull(((com.mybatisflex.core.update.UpdateWrapper) sysUser).getUpdates().get("phone"));
        assertNull(((com.mybatisflex.core.update.UpdateWrapper) sysUser).getUpdates().get("email"));
    }

    @Test
    void 修改_未提交的字段不进updates() {
        User sysUser = UpdateEntity.of(User.class, 1L);
        sysUser.setUsername("lisi");

        UserServiceImpl.normalizeContactUpdates(sysUser);

        // 未提交 phone/email，归一化不得将其加入 updates（避免误 SET NULL）
        assertFalse(((com.mybatisflex.core.update.UpdateWrapper) sysUser).getUpdates().containsKey("phone"));
        assertFalse(((com.mybatisflex.core.update.UpdateWrapper) sysUser).getUpdates().containsKey("email"));
    }

    @Test
    void 修改_非空提交值保留() {
        User sysUser = UpdateEntity.of(User.class, 1L);
        sysUser.setPhone("13912345678");

        UserServiceImpl.normalizeContactUpdates(sysUser);

        assertEquals("13912345678",
                ((com.mybatisflex.core.update.UpdateWrapper) sysUser).getUpdates().get("phone"));
    }
}
