package top.mddata.open.admin.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.entity.DocContent;
import top.mddata.open.admin.mapper.DocContentMapper;
import top.mddata.open.admin.service.DocContentService;

/**
 * 文档内容 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocContentServiceImpl extends SuperServiceImpl<DocContentMapper, DocContent> implements DocContentService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveContent(Long docInfoId, String content) {
        DocContent docContent = this.getOne(QueryWrapper.create().eq(DocContent::getDocInfoId, docInfoId));
        boolean save = false;
        if (docContent == null) {
            save = true;
            docContent = new DocContent();
        }
        docContent.setDocInfoId(docInfoId);
        docContent.setContent(content);
        if (save) {
            this.save(docContent);
        } else {
            this.updateById(docContent);
        }
    }
}
