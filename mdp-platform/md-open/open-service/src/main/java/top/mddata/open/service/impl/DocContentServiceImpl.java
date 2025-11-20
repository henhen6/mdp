package top.mddata.open.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.entity.DocContent;
import top.mddata.open.mapper.DocContentMapper;
import top.mddata.open.service.DocContentService;

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

}
