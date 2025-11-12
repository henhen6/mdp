package top.mddata.console.permission.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.permission.entity.ResourceMenu;
import top.mddata.console.permission.vo.ResourceMenuVo;

import java.util.List;

/**
 * 菜单 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
public interface ResourceMenuService extends SuperService<ResourceMenu> {
    /**
     * 查询系统的全部菜单，菜单权限由前端控制
     *
     * 由于菜单控制需满足以下场景
     * 1. 门户、开放平台文档等系统，在未登录的情况下，需要查询通用菜单
     * 2. 用户登录后，查询当前用户拥有的以及通用的菜单
     * 3. 根据权限控制不同的用户可以访问不同的菜单
     * 4. 对于没有权限的菜单，可以做到：①菜单不可见，访问403； ②菜单可见，访问403
     *
     * @param applicationId 应用ID
     * @return 菜单树
     */
    List<ResourceMenuVo> findAllMenu(Long applicationId);

    /**
     * 检查菜单编码是否重复
     *
     * @param applicationId 应用ID
     * @param code 菜单编码
     * @param id 菜单ID
     * @return true:重复，false:不重复
     */
    Boolean checkCode(Long applicationId, String code, Long id);

    /**
     * 检查菜单路径是否重复
     *
     * @param applicationId 应用ID
     * @param path 菜单路径
     * @param id 菜单ID
     * @return true:重复，false:不重复
     */
    Boolean checkPath(Long applicationId, String path, Long id);

    /**
     * 检查菜单名称是否重复
     *
     * @param applicationId 应用ID
     * @param name 菜单名称
     * @param id 菜单ID
     * @return true:重复，false:不重复
     */
    Boolean checkName(Long applicationId, String name, Long id);

    /**
     * 获取默认菜单
     *
     * @param applicationId 应用ID
     * @param id 父级菜单ID
     * @return 默认菜单
     */
    ResourceMenuVo getDefMenuByParentId(Long applicationId, Long id);

    /**
     * 获取所有子菜单
     *
     * @param parentId 父级菜单ID
     * @return 子菜单
     */
    List<ResourceMenu> findAllChildrenByParentId(Long parentId);

    /**
     * 移动菜单
     *
     * @param sourceId 待移动菜单ID
     * @param targetId 目标菜单ID
     */
    void move(Long sourceId, Long targetId);
}
