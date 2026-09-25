package top.mddata.console.service.organization.accountop.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.service.organization.SystemProtectService;
import top.mddata.console.service.organization.accountop.AccountOperation;
import top.mddata.console.service.organization.accountop.AccountOperationStrategy;
import top.mddata.console.service.organization.accountop.TargetAccountProfile;

/**
 * 运营者策略：可管理一切非运营者目标；运营者目标仅 ops_admin 可管理（平级互不可管）。
 */
@Order(2)
@Component
@RequiredArgsConstructor
public class OperationsAdminStrategy implements AccountOperationStrategy {
    private final SystemProtectService systemProtectService;

    @Override
    public boolean supports(Long operatorId) {
        return systemProtectService.isOperationsAdminUser(operatorId);
    }

    @Override
    public void check(AccountOperation op, TargetAccountProfile target) {
        ArgumentAssert.isFalse(target.isOperationsAdmin(),
                "{}失败：运营者账号仅 ops_admin 可管理", op.getDesc());
    }
}
