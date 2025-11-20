package top.mddata.open.manage.facade;

import top.mddata.base.base.R;
import top.mddata.open.manage.vo.OauthScopeVo;

import java.util.List;

/**
 * 应用权限
 *
 * @author henhen6
 * @since 2025/8/24 23:42
 */
public interface OauthScopeFacade {
    /**
     * 根据应用id查询应用拥有的权限
     *
     * @param appId 应用id
     * @return 权限
     */
    R<List<OauthScopeVo>> listByAppId(Long appId);
}
