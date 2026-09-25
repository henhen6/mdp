package top.mddata.console.service.organization.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.ArgumentException;
import top.mddata.common.constant.BuiltInUserId;
import top.mddata.common.entity.User;
import top.mddata.common.mapper.UserMapper;
import top.mddata.console.dto.organization.UserResetPasswordDto;
import top.mddata.console.dto.organization.UserUpdateDto;
import top.mddata.console.service.organization.accountop.AccountOperation;
import top.mddata.console.service.organization.accountop.AccountOperationGuard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * UserServiceImpl 账号操作矩阵接入测试：state 变化检测 + 重置密码门面委派。
 */
class UserServiceImplAccountOpTest {
    private AutoCloseable mocks;
    @Mock
    private UserMapper mapper;
    @Mock
    private CacheOps cacheOps;
    @Mock
    private AccountOperationGuard accountOperationGuard;
    @Mock
    private top.mddata.console.service.organization.SystemProtectService systemProtectService;
    @Mock
    private top.mddata.open.facade.admin.NotifyAndEventPushFacade notifyAndEventPushFacade;
    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        // guard 命中构造器参数后 Mockito 不再回退字段注入，父类 mapper 需显式设置
        ReflectionTestUtils.setField(userService, "mapper", mapper);
        // removeByIds 会经 SuperServiceImpl.delCache 调用 cacheOps，同样需显式注入
        ReflectionTestUtils.setField(userService, "cacheOps", cacheOps);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    private User dbUser(Long id, Boolean state) {
        User user = new User();
        user.setId(id);
        user.setState(state);
        return user;
    }

    @Test
    void state未传则不触发矩阵校验() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setId(10L);
        userService.updateBefore(dto);
        verifyNoInteractions(accountOperationGuard);
    }

    @Test
    void state未变化则不触发矩阵校验() {
        when(mapper.selectOneById(10L)).thenReturn(dbUser(10L, true));
        UserUpdateDto dto = new UserUpdateDto();
        dto.setId(10L);
        dto.setState(true);
        userService.updateBefore(dto);
        verifyNoInteractions(accountOperationGuard);
    }

    @Test
    void 禁用变化则触发DISABLE校验() {
        when(mapper.selectOneById(10L)).thenReturn(dbUser(10L, true));
        UserUpdateDto dto = new UserUpdateDto();
        dto.setId(10L);
        dto.setState(false);
        userService.updateBefore(dto);
        verify(accountOperationGuard).check(10L, AccountOperation.DISABLE);
    }

    @Test
    void 启用变化则触发ENABLE校验() {
        when(mapper.selectOneById(10L)).thenReturn(dbUser(10L, false));
        UserUpdateDto dto = new UserUpdateDto();
        dto.setId(10L);
        dto.setState(true);
        userService.updateBefore(dto);
        verify(accountOperationGuard).check(10L, AccountOperation.ENABLE);
    }

    @Test
    void 重置密码委派门面且门面拒绝时不写库() {
        UserResetPasswordDto data = new UserResetPasswordDto();
        data.setId(20L);
        data.setDefPassword(true);
        doThrow(new ArgumentException("重置密码失败：无权操作"))
                .when(accountOperationGuard).check(20L, AccountOperation.RESET_PASSWORD);
        assertThrows(ArgumentException.class, () -> userService.resetPassword(data));
        verify(mapper, never()).update(any(User.class), anyBoolean());
    }

    @Test
    void 删除用户逐id委派门面校验() {
        userService.removeByIds(List.of(31L, 32L));
        verify(accountOperationGuard).check(31L, AccountOperation.DELETE);
        verify(accountOperationGuard).check(32L, AccountOperation.DELETE);
    }

    @Test
    void 删除内置账号被门面拒绝则不删库() {
        doThrow(new ArgumentException("删除失败：该账号是系统内置账号，禁止删除"))
                .when(accountOperationGuard)
                .check(BuiltInUserId.OPS_ADMIN, AccountOperation.DELETE);
        assertThrows(ArgumentException.class,
                () -> userService.removeByIds(List.of(BuiltInUserId.OPS_ADMIN)));
        verify(accountOperationGuard).check(BuiltInUserId.OPS_ADMIN, AccountOperation.DELETE);
        verify(mapper, never()).deleteBatchByIds(anyCollection());
    }
}
