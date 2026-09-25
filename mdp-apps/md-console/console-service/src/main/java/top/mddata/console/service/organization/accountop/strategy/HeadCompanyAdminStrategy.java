package top.mddata.console.service.organization.accountop.strategy;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.constant.BuiltInUserId;
import top.mddata.console.service.organization.accountop.AccountOperation;
import top.mddata.console.service.organization.accountop.AccountOperationStrategy;
import top.mddata.console.service.organization.accountop.TargetAccountProfile;

/**
 * admin 策略：仅可管理总公司树内的账号。
 */
@Order(4)
@Component
public class HeadCompanyAdminStrategy implements AccountOperationStrategy {

    @Override
    public boolean supports(Long operatorId) {
        return operatorId != null && operatorId == BuiltInUserId.ADMIN;
    }

    @Override
    public void check(AccountOperation op, TargetAccountProfile target) {
        ArgumentAssert.isTrue(target.isInHeadCompanyTree(),
                "{}失败：admin 仅可管理总公司下的账号", op.getDesc());
    }
}
