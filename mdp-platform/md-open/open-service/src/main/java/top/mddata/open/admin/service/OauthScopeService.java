package top.mddata.open.admin.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.admin.entity.OauthScope;
import top.mddata.open.admin.vo.OauthScopeVo;

import java.util.List;

/**
 * oauth2权限 服务层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
public interface OauthScopeService extends SuperService<OauthScope> {
    /**
     * 检测权限编码是否存在
     *
     * @param code 权限编码
     * @param id   权限id
     * @return true 存在， false 不存在
     */
    boolean check(String code, Long id);

    /**
     * 查询最大的排序
     *
     * @return 排序
     */
    long getMaxWeight();

    /**
     * 根据权限编码查询 应用权限
     *
     * @param scopes 权限编码
     * @return 应用权限
     */
    List<OauthScopeVo> getScopeListByCode(List<String> scopes);

    /**
     * 根据应用id查询应用拥有的权限
     *
     * @param appId 应用id
     * @return 权限
     */
    List<OauthScopeVo> listByAppId(Long appId);
}
