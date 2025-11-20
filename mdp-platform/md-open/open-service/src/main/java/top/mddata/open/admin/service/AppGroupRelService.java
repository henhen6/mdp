package top.mddata.open.admin.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.admin.entity.AppGroupRel;

import java.util.List;

/**
 * 应用拥有的权限分组 服务层。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
public interface AppGroupRelService extends SuperService<AppGroupRel> {

    /**
     * 根据应用ID查询拥有的权限组
     *
     * @param appId 应用ID
     * @return 权限组ID列表
     */
    List<Long> listGroupIdByAppId(Long appId);
}
