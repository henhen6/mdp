package top.mddata.open.manage.facade;

import top.mddata.base.base.R;
import top.mddata.open.admin.vo.AppVo;
import top.mddata.open.admin.vo.OauthScopeVo;

import java.util.List;

/**
 * 应用接口
 *
 * @author henhen6
 * @since 2025/8/12 11:24
 */
public interface AppFacade {
    /**
     * 查询需要 接收事件推送的应用
     *
     * @return 应用
     */
    R<List<AppVo>> listNeedPushApp();

    /**
     * 根据权限编码查询 应用权限
     *
     * @param scopes 权限编码
     * @return 应用权限
     */
    R<List<OauthScopeVo>> getScopeListByCode(List<String> scopes);

    /**
     * 根据id查询应用
     *
     * @param appKey 应用标识
     * @return 应用
     */
    R<AppVo> getByAppKey(String appKey);

}
