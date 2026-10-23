package top.mddata.open.service.oauth;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.entity.admin.OauthOpenid;
import top.mddata.open.vo.admin.OauthOpenidVo;

/**
 * openid 服务层。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
public interface OauthOpenidService extends SuperService<OauthOpenid> {
    /**
     * 根据应用标识查询用户的openid
     * @param appKey 应用标识
     * @param userId 用户id
     * @return OpenId
     */
    OauthOpenidVo getByAppKeyAndUserId(String appKey, Long userId);
}
