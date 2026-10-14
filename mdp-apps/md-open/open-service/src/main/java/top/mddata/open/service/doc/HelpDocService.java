package top.mddata.open.service.doc;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.entity.admin.HelpDoc;

import java.util.List;

/**
 * 帮助文档 服务层。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
public interface HelpDocService extends SuperService<HelpDoc> {
    /**
     * 根据父ID查询所有的子节点
     *
     * @param parentId 父id
     * @return
     */
    List<HelpDoc> findAllChildrenByParentId(Long parentId);

    /**
     * 移动节点
     * @param sourceId 源节点ID
     * @param targetId 父节点ID
     */
    void move(Long sourceId, Long targetId);

}
