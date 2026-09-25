package top.mddata.console.service.organization.accountop.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.exception.ArgumentException;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.constant.BuiltInOrgId;
import top.mddata.common.constant.BuiltInUserId;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.UserOrgRel;
import top.mddata.common.mapper.OrgMapper;
import top.mddata.console.service.organization.SystemProtectService;
import top.mddata.console.service.organization.accountop.AccountOperation;
import top.mddata.console.service.organization.accountop.AccountOperationGuard;
import top.mddata.console.service.organization.accountop.AccountOperationStrategy;
import top.mddata.console.service.organization.accountop.TargetAccountProfile;

import java.util.List;

/**
 * 账号管理操作门面实现：通用规则前置拦截 + 策略调度。
 *
 * <p>策略列表由 Spring 按 @Order 排序注入；
 * 本类只依赖 Mapper 与 SystemProtectService 接口，避免与业务 Service 形成循环依赖。</p>
 */
@Service
@RequiredArgsConstructor
public class AccountOperationGuardImpl implements AccountOperationGuard {
    private final List<AccountOperationStrategy> strategies;
    private final SystemProtectService systemProtectService;
    private final OrgMapper orgMapper;

    @Override
    @Transactional(readOnly = true)
    public void check(Long targetId, AccountOperation op) {
        ArgumentAssert.notNull(targetId, "{}失败：目标账号不能为空", op.getDesc());
        Long operatorId = ContextUtil.getUserId();

        // 通用规则：内置账号删除绝对禁止（置于自身规则之前，ops_admin 删自己也命中本规则）。
        // 非内置账号的删除不做矩阵限制，直接放行，保持现有删除语义
        if (AccountOperation.DELETE == op) {
            ArgumentAssert.isFalse(BuiltInUserId.ALL.contains(targetId),
                    "{}失败：该账号是系统内置账号，禁止删除", op.getDesc());
            return;
        }
        // 通用规则：目标是自己。禁用自己一律拒绝（防自我锁死）；启用/重置自己直接放行
        if (targetId.equals(operatorId)) {
            ArgumentAssert.isFalse(AccountOperation.DISABLE == op,
                    "{}失败：不能禁用自己的账号", op.getDesc());
            return;
        }
        // 通用规则：目标是 ops_admin。禁用/重置一律拒绝（本人已在上面放行）；启用放行
        if (BuiltInUserId.OPS_ADMIN == targetId) {
            ArgumentAssert.isFalse(AccountOperation.DISABLE == op,
                    "{}失败：ops_admin 是系统最高账号，禁止禁用", op.getDesc());
            ArgumentAssert.isFalse(AccountOperation.RESET_PASSWORD == op,
                    "{}失败：ops_admin 的密码仅本人可重置", op.getDesc());
            return;
        }

        TargetAccountProfile target = buildProfile(targetId);
        for (AccountOperationStrategy strategy : strategies) {
            if (strategy.supports(operatorId)) {
                strategy.check(op, target);
                return;
            }
        }
        throw new ArgumentException("{}失败：当前账号无权操作其他账号", op.getDesc());
    }

    /**
     * 组装目标账号画像：运营者身份 + 所属组织树归属（开发者平台树 / 总公司树）
     */
    private TargetAccountProfile buildProfile(Long targetId) {
        List<Org> orgList = orgMapper.selectListByQuery(QueryWrapper.create()
                .select(Org::getTreePath)
                .from(Org.class)
                .innerJoin(UserOrgRel.class).on(UserOrgRel::getOrgId, Org::getId)
                .where(UserOrgRel::getUserId).eq(targetId));
        return TargetAccountProfile.builder()
                .userId(targetId)
                .operationsAdmin(systemProtectService.isOperationsAdminUser(targetId))
                .inDeveloperTree(inTree(orgList, BuiltInOrgId.DEVELOPER_PLATFORM))
                .inHeadCompanyTree(inTree(orgList, BuiltInOrgId.HEAD_COMPANY))
                .build();
    }

    private boolean inTree(List<Org> orgList, long rootId) {
        return orgList.stream().map(Org::getTreePath)
                .anyMatch(path -> path != null && path.startsWith("/" + rootId + "/"));
    }
}
