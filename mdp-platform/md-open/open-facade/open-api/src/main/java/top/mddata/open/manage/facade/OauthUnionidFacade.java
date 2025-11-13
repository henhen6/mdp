package top.mddata.open.manage.facade;

import top.mddata.base.base.R;
import top.mddata.open.manage.vo.OauthUnionidVo;

/**
 * Unionid
 *
 * @author henhen6
 * @since 2025/8/21 23:33
 */
public interface OauthUnionidFacade {
    /**
     * 根据主体id和用户id 查询union
     *
     * @param subjectId 主体id
     * @param userId    用户id
     * @return union
     */
    R<OauthUnionidVo> getBySubjectIdAndUserId(Long subjectId, Long userId);
}
