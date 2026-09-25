package top.mddata.console.service.organization.accountop.strategy;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.constant.BuiltInUserId;
import top.mddata.console.service.organization.accountop.AccountOperation;
import top.mddata.console.service.organization.accountop.AccountOperationStrategy;
import top.mddata.console.service.organization.accountop.TargetAccountProfile;

/**
 * open_admin 策略：仅可管理开发者平台树内的账号。
 */
@Order(3)
@Component
public class OpenAdminStrategy implements AccountOperationStrategy {

    @Override
    public boolean supports(Long operatorId) {
        return operatorId != null && operatorId == BuiltInUserId.OPEN_ADMIN;
    }

    @Override
    public void check(AccountOperation op, TargetAccountProfile target) {
        ArgumentAssert.isTrue(target.isInDeveloperTree(),
                "{}失败：open_admin 仅可管理开发者平台下的账号", op.getDesc());
    }
}
