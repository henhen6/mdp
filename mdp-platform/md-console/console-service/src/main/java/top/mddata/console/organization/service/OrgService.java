package top.mddata.console.organization.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.common.entity.Org;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 组织 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 15:49:10
 */
public interface OrgService extends SuperService<Org> {

    /**
     * 移动组织架构
     * @param sourceId 待移动的id
     * @param targetId 目标id
     */
    void move(Long sourceId, Long targetId);

    /**
     * 根据父id 递归查询所有的子集
     * @param parentId 父id
     * @return
     */
    List<Org> findAllChildrenByParentId(Long parentId);

    /**
     * 根据id查询待回显参数
     *
     * @param ids 唯一键（可能不是主键ID)
     * @return 回显数据
     */
    Map<Serializable, Object> findByIds(Set<Serializable> ids);
}
