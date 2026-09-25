package top.mddata.console.service.organization.accountop.strategy;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import top.mddata.common.constant.BuiltInUserId;
import top.mddata.console.service.organization.accountop.AccountOperation;
import top.mddata.console.service.organization.accountop.AccountOperationStrategy;
import top.mddata.console.service.organization.accountop.TargetAccountProfile;

/**
 * ops_admin 策略：系统最高账号，放行一切目标。
 *
 * <p>自身目标与 ops_admin 目标已被门面通用规则拦截，到达本策略的目标无需再校验。</p>
 */
@Order(1)
@Component
public class OpsAdminStrategy implements AccountOperationStrategy {

    @Override
    public boolean supports(Long operatorId) {
        return operatorId != null && operatorId == BuiltInUserId.OPS_ADMIN;
    }

    @Override
    public void check(AccountOperation op, TargetAccountProfile target) {
        // ops_admin 无任何目标限制
    }
}
