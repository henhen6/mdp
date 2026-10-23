package top.mddata.open.service.app;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.dto.admin.AppScopeApplyDto;
import top.mddata.open.dto.admin.AppScopeApplyReviewDto;
import top.mddata.open.entity.admin.AppScopeApply;

import java.util.List;

/**
 * 权限组申请 服务层。
 *
 * @author henhen6
 */
public interface AppScopeApplyService extends SuperService<AppScopeApply> {

    /**
     * 批量申请权限组（一单一组，循环生成多张待审批申请单）。
     * 校验：应用归属当前用户、组存在且启用、未拥有、无在审单。
     *
     * @param dto 申请参数
     * @return 成功生成的申请单数量
     */
    Integer apply(AppScopeApplyDto dto);

    /**
     * 审核申请单。通过：写入 mdo_app_group_rel（幂等）；退回：审核意见必填。
     *
     * @param dto 审核参数
     * @return 是否成功
     */
    Boolean review(AppScopeApplyReviewDto dto);

    /**
     * 撤销申请单（仅待审批状态可撤销，限本人应用的申请单）。
     *
     * @param id 申请单ID
     * @return 是否成功
     */
    Boolean withdraw(Long id);

    /**
     * 删除申请单（仅已撤销状态可删除，限本人应用的申请单）。
     *
     * @param ids 申请单ID列表
     * @return 是否成功
     */
    Boolean deleteByIds(List<Long> ids);
}
