package top.mddata.console.service.organization.accountop;

import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import top.mddata.base.exception.ArgumentException;
import top.mddata.base.util.ContextUtil;
import top.mddata.common.constant.BuiltInOrgId;
import top.mddata.common.constant.BuiltInUserId;
import top.mddata.common.entity.Org;
import top.mddata.common.mapper.OrgMapper;
import top.mddata.console.service.organization.SystemProtectService;
import top.mddata.console.service.organization.accountop.impl.AccountOperationGuardImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 账号操作门面测试：通用规则（自身、ops_admin 目标）+ 策略调度 + 目标画像组装。
 */
class AccountOperationGuardImplTest {
    private AutoCloseable mocks;
    @Mock
    private AccountOperationStrategy strategyA;
    @Mock
    private AccountOperationStrategy strategyB;
    @Mock
    private SystemProtectService systemProtectService;
    @Mock
    private OrgMapper orgMapper;
    private AccountOperationGuardImpl guard;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        guard = new AccountOperationGuardImpl(
                List.of(strategyA, strategyB), systemProtectService, orgMapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        ContextUtil.remove();
        mocks.close();
    }

    @Test
    void 目标账号为空则拒绝() {
        ContextUtil.setUserId(1L);
        assertThrows(ArgumentException.class,
                () -> guard.check(null, AccountOperation.DISABLE));
    }

    @Test
    void 禁用自己则拒绝() {
        ContextUtil.setUserId(10L);
        ArgumentException e = assertThrows(ArgumentException.class,
                () -> guard.check(10L, AccountOperation.DISABLE));
        assertTrue(e.getMessage().contains("不能禁用自己的账号"));
        verifyNoInteractions(strategyA, strategyB, orgMapper);
    }

    @Test
    void 重置自己的密码与启用自己则放行() {
        ContextUtil.setUserId(10L);
        assertDoesNotThrow(() -> guard.check(10L, AccountOperation.RESET_PASSWORD));
        assertDoesNotThrow(() -> guard.check(10L, AccountOperation.ENABLE));
        verifyNoInteractions(strategyA, strategyB, orgMapper);
    }

    @Test
    void 目标是opsAdmin则禁用与重置均拒绝() {
        ContextUtil.setUserId(1L);
        ArgumentException e1 = assertThrows(ArgumentException.class,
                () -> guard.check(BuiltInUserId.OPS_ADMIN, AccountOperation.DISABLE));
        assertTrue(e1.getMessage().contains("ops_admin 是系统最高账号，禁止禁用"));
        ArgumentException e2 = assertThrows(ArgumentException.class,
                () -> guard.check(BuiltInUserId.OPS_ADMIN, AccountOperation.RESET_PASSWORD));
        assertTrue(e2.getMessage().contains("ops_admin 的密码仅本人可重置"));
        verifyNoInteractions(strategyA, strategyB, orgMapper);
    }

    @Test
    void 目标是opsAdmin则启用放行() {
        ContextUtil.setUserId(1L);
        assertDoesNotThrow(() -> guard.check(BuiltInUserId.OPS_ADMIN, AccountOperation.ENABLE));
        verifyNoInteractions(strategyA, strategyB, orgMapper);
    }

    @Test
    void 首个supports命中的策略执行且后续策略不再询问() {
        ContextUtil.setUserId(2L);
        when(strategyA.supports(2L)).thenReturn(true);
        guard.check(20L, AccountOperation.DISABLE);
        ArgumentCaptor<TargetAccountProfile> captor = ArgumentCaptor.forClass(TargetAccountProfile.class);
        verify(strategyA).check(eq(AccountOperation.DISABLE), captor.capture());
        assertEquals(20L, captor.getValue().getUserId());
        verify(strategyB, never()).supports(any());
    }

    @Test
    void 首个策略不适用则询问后续策略() {
        ContextUtil.setUserId(2L);
        when(strategyA.supports(2L)).thenReturn(false);
        when(strategyB.supports(2L)).thenReturn(true);
        guard.check(20L, AccountOperation.RESET_PASSWORD);
        verify(strategyB).check(eq(AccountOperation.RESET_PASSWORD), any(TargetAccountProfile.class));
    }

    @Test
    void 无策略命中则拒绝() {
        ContextUtil.setUserId(2L);
        when(strategyA.supports(2L)).thenReturn(false);
        when(strategyB.supports(2L)).thenReturn(false);
        ArgumentException e = assertThrows(ArgumentException.class,
                () -> guard.check(20L, AccountOperation.DISABLE));
        assertTrue(e.getMessage().contains("无权操作其他账号"));
    }

    @Test
    void 目标画像按组织树与运营者身份组装() {
        ContextUtil.setUserId(2L);
        when(strategyA.supports(2L)).thenReturn(true);
        when(systemProtectService.isOperationsAdminUser(20L)).thenReturn(true);
        Org devOrg = new Org();
        devOrg.setTreePath("/" + BuiltInOrgId.DEVELOPER_PLATFORM + "/688000000000000001/");
        when(orgMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(devOrg));

        guard.check(20L, AccountOperation.DISABLE);

        ArgumentCaptor<TargetAccountProfile> captor = ArgumentCaptor.forClass(TargetAccountProfile.class);
        verify(strategyA).check(eq(AccountOperation.DISABLE), captor.capture());
        TargetAccountProfile profile = captor.getValue();
        assertTrue(profile.isOperationsAdmin());
        assertTrue(profile.isInDeveloperTree());
        assertFalse(profile.isInHeadCompanyTree());
    }

    @Test
    void 删除内置账号一律拒绝() {
        ContextUtil.setUserId(BuiltInUserId.OPS_ADMIN);
        for (Long builtInId : BuiltInUserId.ALL) {
            ArgumentException e = assertThrows(ArgumentException.class,
                    () -> guard.check(builtInId, AccountOperation.DELETE));
            assertTrue(e.getMessage().contains("系统内置账号，禁止删除"));
        }
        verifyNoInteractions(strategyA, strategyB, orgMapper, systemProtectService);
    }

    @Test
    void 删除非内置账号放行且不走策略不查库() {
        ContextUtil.setUserId(10L);
        assertDoesNotThrow(() -> guard.check(20L, AccountOperation.DELETE));
        verifyNoInteractions(strategyA, strategyB, orgMapper);
    }
}
