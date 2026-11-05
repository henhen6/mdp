package top.mddata.console.controller;

import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.base.exception.ArgumentException;
import top.mddata.base.interfaces.echo.EchoService;
import top.mddata.common.entity.User;
import top.mddata.common.mapper.UserMapper;
import top.mddata.console.controller.organization.OrgController;
import top.mddata.console.controller.organization.UserController;
import top.mddata.console.controller.permission.RoleController;
import top.mddata.console.service.organization.OrgService;
import top.mddata.console.service.organization.OrgVisibilityService;
import top.mddata.console.service.organization.UserService;
import top.mddata.console.service.permission.RoleDataScopeRelService;
import top.mddata.console.service.permission.RoleFieldRelService;
import top.mddata.console.service.permission.RoleResourceRelService;
import top.mddata.console.service.permission.RoleService;
import top.mddata.console.vo.organization.OrgVo;
import top.mddata.console.vo.permission.RoleVo;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 用户、组织、角色按ID批量回显接口的控制器测试。
 *
 * <p>覆盖：空参数与超量参数 fail fast；重复ID去重后单次 IN 查询；
 * 用户走关系查询并保留可见性过滤；组织保留可见性过滤；角色不附加组织性质条件；三者均执行 Echo。</p>
 */
@ExtendWith(MockitoExtension.class)
class BatchEchoControllerTest {

    /**
     * 提取 SQL 中 IN (...) 括号内的值列表
     */
    private static final Pattern IN_VALUES_PATTERN = Pattern.compile("(?i)IN \\(([^)]*)\\)");

    @Mock
    private EchoService echoService;
    @Mock
    private OrgVisibilityService orgVisibilityService;
    @Mock
    private RoleService roleService;
    @Mock
    private UserService userService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private OrgService orgService;
    @Mock
    private RoleResourceRelService roleResourceRelService;
    @Mock
    private RoleFieldRelService roleFieldRelService;
    @Mock
    private RoleDataScopeRelService roleDataScopeRelService;

    @Test
    void userListByIdsRejectsEmptyIds() {
        UserController controller = newUserController();

        assertThrows(ArgumentException.class, () -> controller.listByIds(List.of()));

        verifyNoInteractions(userService);
        verifyNoInteractions(echoService);
    }

    @Test
    void userListByIdsUsesSingleRelationQueryAndEcho() {
        UserController controller = newUserController();
        ReflectionTestUtils.setField(controller, "superService", userService);
        when(orgVisibilityService.currentVisibleRootOrgIds()).thenReturn(null);
        doReturn(userMapper).when(userService).getMapper();
        doReturn(List.of(new User())).when(userMapper).selectListWithRelationsByQuery(any());

        controller.listByIds(List.of(1L, 1L, 2L));

        ArgumentCaptor<QueryWrapper> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(userMapper, times(1)).selectListWithRelationsByQuery(captor.capture());
        verify(echoService).action(anyList());
        verify(userService, never()).getById(any());
        String sql = captor.getValue().toSQL();
        assertThat(sql).contains("id").containsIgnoringCase("in");
        // mybatis-flex 的 toSQL 将 IN 参数内联，提取括号内的值数量验证重复ID已去重（3 个入参只剩 2 个值）
        Matcher inMatcher = IN_VALUES_PATTERN.matcher(sql);
        assertThat(inMatcher.find()).as("SQL 应包含单次 IN 查询: %s", sql).isTrue();
        long inValueCount = Arrays.stream(inMatcher.group(1).split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .count();
        assertThat(inValueCount).isEqualTo(2);
    }

    @Test
    void orgListByIdsUsesSingleQueryWithVisibilityAndEcho() {
        OrgController controller = newOrgController();
        ReflectionTestUtils.setField(controller, "superService", orgService);
        when(orgVisibilityService.currentVisibleRootOrgIds()).thenReturn(null);
        doReturn(List.of()).when(orgService).listAs(any(), eq(OrgVo.class));

        controller.listByIds(List.of(2L, 2L, 3L));

        verify(orgService, times(1)).listAs(any(QueryWrapper.class), eq(OrgVo.class));
        verify(echoService).action(anyList());
        verify(orgVisibilityService).currentVisibleRootOrgIds();
        verify(orgService, never()).getById(any());
    }

    @Test
    void roleListByIdsUsesSingleQueryAndEchoWithoutOrgNatureFilter() {
        RoleController controller = newRoleController();
        ReflectionTestUtils.setField(controller, "superService", roleService);
        doReturn(List.of()).when(roleService).listAs(any(), eq(RoleVo.class));

        controller.listByIds(List.of(4L, 4L, 5L));

        verify(roleService, times(1)).listAs(any(QueryWrapper.class), eq(RoleVo.class));
        verify(echoService).action(anyList());
        verify(roleService, never()).getById(any());
        verifyNoInteractions(roleResourceRelService, roleFieldRelService, roleDataScopeRelService);
    }

    @Test
    void emptyIdsRejectedByAllControllers() {
        assertThrows(ArgumentException.class, () -> newOrgController().listByIds(List.of()));
        assertThrows(ArgumentException.class, () -> newRoleController().listByIds(List.of()));
    }

    @Test
    void batchListByIdsRejectsMoreThanOneThousandIds() {
        List<Long> ids = LongStream.rangeClosed(1, 1001).boxed().toList();

        assertThrows(ArgumentException.class, () -> newUserController().listByIds(ids));
        assertThrows(ArgumentException.class, () -> newOrgController().listByIds(ids));
        assertThrows(ArgumentException.class, () -> newRoleController().listByIds(ids));

        verifyNoInteractions(userService, orgService, roleService);
    }

    private UserController newUserController() {
        return new UserController(echoService, orgVisibilityService, roleService);
    }

    private OrgController newOrgController() {
        return new OrgController(echoService, orgVisibilityService);
    }

    private RoleController newRoleController() {
        return new RoleController(roleResourceRelService, roleFieldRelService,
                roleDataScopeRelService, echoService);
    }
}
