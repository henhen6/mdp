package top.mddata.open.service.app.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.enumeration.AuditStatusEnum;
import top.mddata.open.dto.admin.AppScopeApplyDto;
import top.mddata.open.dto.admin.AppScopeApplyReviewDto;
import top.mddata.open.entity.admin.App;
import top.mddata.open.entity.admin.AppGroupRel;
import top.mddata.open.entity.admin.AppScopeApply;
import top.mddata.open.entity.admin.ScopeGroup;
import top.mddata.open.mapper.admin.AppScopeApplyMapper;
import top.mddata.open.service.app.AppGroupRelService;
import top.mddata.open.service.app.AppScopeApplyService;
import top.mddata.open.service.app.AppService;
import top.mddata.open.service.oauth.ScopeGroupService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限组申请 服务层实现。
 *
 * @author henhen6
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppScopeApplyServiceImpl
        extends SuperServiceImpl<AppScopeApplyMapper, AppScopeApply>
        implements AppScopeApplyService {

    private final AppService appService;
    private final ScopeGroupService scopeGroupService;
    private final AppGroupRelService appGroupRelService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer apply(AppScopeApplyDto dto) {
        // 归属校验：只能为本人创建的应用申请
        long appCount = appService.count(QueryWrapper.create()
                .eq(App::getId, dto.getAppId())
                .eq(App::getCreatedBy, ContextUtil.getUserId()));
        ArgumentAssert.isTrue(appCount > 0, "应用不存在或无权限操作");

        List<AppScopeApply> applyList = new ArrayList<>();
        for (Long groupId : dto.getGroupIds()) {
            ScopeGroup group = scopeGroupService.getById(groupId);
            ArgumentAssert.notNull(group, "权限组[{}]不存在", groupId);
            ArgumentAssert.isTrue(Boolean.TRUE.equals(group.getState()),
                    "权限组[{}]已停用", group.getName());

            long owned = appGroupRelService.count(QueryWrapper.create()
                    .eq(AppGroupRel::getAppId, dto.getAppId())
                    .eq(AppGroupRel::getGroupId, groupId));
            ArgumentAssert.isTrue(owned == 0, "已拥有权限组[{}]，请勿重复申请",
                    group.getName());

            long pending = count(QueryWrapper.create()
                    .eq(AppScopeApply::getAppId, dto.getAppId())
                    .eq(AppScopeApply::getGroupId, groupId)
                    .eq(AppScopeApply::getAuditStatus, AuditStatusEnum.PENDING.getCode()));
            ArgumentAssert.isTrue(pending == 0,
                    "权限组[{}]存在待审批的申请，请勿重复提交", group.getName());

            AppScopeApply apply = new AppScopeApply();
            apply.setAppId(dto.getAppId());
            apply.setGroupId(groupId);
            apply.setApplyReason(dto.getApplyReason());
            apply.setAuditStatus(AuditStatusEnum.PENDING.getCode());
            applyList.add(apply);
        }
        saveBatch(applyList);
        return applyList.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean review(AppScopeApplyReviewDto dto) {
        AppScopeApply apply = getById(dto.getId());
        ArgumentAssert.notNull(apply, "申请单不存在");
        ArgumentAssert.isTrue(AuditStatusEnum.PENDING.eq(apply.getAuditStatus()),
                "申请单状态已变更，请刷新后重试");

        if (dto.getApprove()) {
            // 幂等：已存在授权关系则跳过写入，防止并发/重复审核产生重复数据
            long exists = appGroupRelService.count(QueryWrapper.create()
                    .eq(AppGroupRel::getAppId, apply.getAppId())
                    .eq(AppGroupRel::getGroupId, apply.getGroupId()));
            if (exists == 0) {
                AppGroupRel rel = new AppGroupRel();
                rel.setAppId(apply.getAppId());
                rel.setGroupId(apply.getGroupId());
                appGroupRelService.save(rel);
            }
            updateAudit(apply.getId(), AuditStatusEnum.APPROVED, dto.getReviewComments());
        } else {
            ArgumentAssert.notBlank(dto.getReviewComments(), "退回时必须填写审核意见");
            updateAudit(apply.getId(), AuditStatusEnum.REJECTED, dto.getReviewComments());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean withdraw(Long id) {
        AppScopeApply apply = getOwnedApply(id);
        ArgumentAssert.isTrue(AuditStatusEnum.PENDING.eq(apply.getAuditStatus()),
                "仅待审批的申请可以撤销");
        updateAudit(apply.getId(), AuditStatusEnum.WITHDRAWN, apply.getReviewComments());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<Long> ids) {
        ArgumentAssert.notEmpty(ids, "请选择要删除的申请单");
        for (Long id : ids) {
            AppScopeApply apply = getOwnedApply(id);
            ArgumentAssert.isTrue(AuditStatusEnum.WITHDRAWN.eq(apply.getAuditStatus()),
                    "仅已撤销的申请可以删除");
        }
        return removeByIds(ids);
    }

    /**
     * 查询申请单并校验归属：只能操作本人创建应用的申请单。
     */
    private AppScopeApply getOwnedApply(Long id) {
        AppScopeApply apply = getById(id);
        ArgumentAssert.notNull(apply, "申请单不存在");
        long appCount = appService.count(QueryWrapper.create()
                .eq(App::getId, apply.getAppId())
                .eq(App::getCreatedBy, ContextUtil.getUserId()));
        ArgumentAssert.isTrue(appCount > 0, "申请单不存在或无权限操作");
        return apply;
    }

    private void updateAudit(Long id, AuditStatusEnum status, String reviewComments) {
        AppScopeApply update = new AppScopeApply();
        update.setId(id);
        update.setAuditStatus(status.getCode());
        update.setReviewComments(reviewComments);
        update.setAuditAt(LocalDateTime.now());
        updateById(update);
    }
}
