package top.mddata.console.permission.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.permission.entity.Role;

/**
 * 角色 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
public interface RoleService extends SuperService<Role> {
    /**
     * 检测角色编码是否已存在
     * @param roleCategory 角色类别
     * @param code 角色编码
     * @param id 角色ID
     * @return true-已存在，false-不存在
     */
    Boolean checkCode(String roleCategory, String code, Long id);

    /**
     * 检测角色类别和组织性质是否已经存在
     * @param roleCategory 角色类别
     * @param orgNature 组织性质
     * @param id 角色ID
     * @return true-已存在，false-不存在
     */
    Boolean checkCategoryAndOrgNature(String roleCategory, Integer orgNature, Long id);


}
