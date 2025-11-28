package top.mddata.open.admin.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.admin.dto.AppApplyReviewDto;
import top.mddata.open.admin.entity.AppApply;

/**
 * 应用申请 服务层。
 *
 * @author henhen6
 * @since 2025-11-27 03:31:55
 */
public interface AppApplyService extends SuperService<AppApply> {

    /**
     * 应用审核
     * @param dto 审核参数
     * @return 是否成功
     */
    Boolean review(AppApplyReviewDto dto);
}
