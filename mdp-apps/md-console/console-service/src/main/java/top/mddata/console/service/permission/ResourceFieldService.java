package top.mddata.console.service.permission;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.entity.permission.ResourceField;

import java.io.Serializable;
import java.util.Collection;

/**
 * 字段权限 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
public interface ResourceFieldService extends SuperService<ResourceField> {

    /**
     * 按菜单删除字段规则配置（删除菜单时级联调用）：
     * 删除字段规则与角色字段关系，并失效 uri-menu 预解析缓存与相关用户的字段受限集缓存
     *
     * @param menuIdList 菜单ID集合
     * @return true-存在并删除了字段规则；false-菜单下无字段规则
     */
    Boolean deleteByMenuIds(Collection<? extends Serializable> menuIdList);

}
