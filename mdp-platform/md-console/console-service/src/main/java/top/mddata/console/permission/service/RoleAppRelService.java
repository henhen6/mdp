package top.mddata.console.permission.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.permission.dto.RoleAppRelDto;
import top.mddata.console.permission.entity.RoleAppRel;

/**
 * 角色应用关联 服务层。
 *
 * @author henhen6
 * @since 2025-12-03 14:54:25
 */
public interface RoleAppRelService extends SuperService<RoleAppRel> {

    /**
     * 删除 角色和应用的关联
     * @param dto 参数
     * @return 是否成功
     */
    Boolean delete(RoleAppRelDto dto);

    /**
     * 给角色授权应用
     * @param dto 参数
     * @return 是否成功
     */
    Boolean saveByDto(RoleAppRelDto dto);
}
