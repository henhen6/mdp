package top.mddata.open.manage.facade;

import top.mddata.base.base.R;
import top.mddata.open.admin.vo.OauthOpenidVo;

/**
 * Openid
 *
 * @author henhen6
 * @since 2025/8/21 23:33
 */
public interface OauthOpenidFacade {
    /**
     * 根据应用id和用户id 查询union
     *
     * @param appKey  应用id
     * @param userId 用户id
     * @return union
     */
    R<OauthOpenidVo> getByAppKeyAndUserId(String appKey, Long userId);
}
