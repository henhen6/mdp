package top.mddata.console.service.organization.accountop;

import lombok.Builder;
import lombok.Getter;

/**
 * 目标账号画像。
 *
 * <p>由 AccountOperationGuard 一次性查询组装后传入策略，保证策略内零查询、可纯单测。</p>
 */
@Getter
@Builder
public class TargetAccountProfile {
    /**
     * 目标账号id
     */
    private final Long userId;
    /**
     * 是否运营者（持有启用状态的运营管理员角色）
     */
    private final boolean operationsAdmin;
    /**
     * 是否挂在开发者平台树内
     */
    private final boolean inDeveloperTree;
    /**
     * 是否挂在总公司树内
     */
    private final boolean inHeadCompanyTree;
}
