package top.mddata.open.admin.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.admin.entity.DocContent;

/**
 * 文档内容 服务层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
public interface DocContentService extends SuperService<DocContent> {
    /**
     * 保存文档内容
     * @param docInfoId 文档id
     * @param content 文档内容
     */
    void saveContent(Long docInfoId, String content);
}
