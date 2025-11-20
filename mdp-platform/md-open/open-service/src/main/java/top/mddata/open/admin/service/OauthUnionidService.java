package top.mddata.open.admin.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.admin.entity.OauthUnionid;
import top.mddata.open.admin.vo.OauthUnionidVo;

/**
 * unionid 服务层。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
public interface OauthUnionidService extends SuperService<OauthUnionid> {
    /**
     * 根据主体id和用户id 查询union
     *
     * @param subjectId 主体id
     * @param userId    用户id
     * @return union
     */
    OauthUnionidVo getBySubjectIdAndUserId(Long subjectId, Long userId);
}
