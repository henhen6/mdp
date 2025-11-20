package top.mddata.open.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.common.enumeration.BooleanEnum;
import top.mddata.open.admin.entity.DocContent;
import top.mddata.open.admin.entity.DocGroup;
import top.mddata.open.admin.entity.DocInfo;
import top.mddata.open.admin.mapper.DocGroupMapper;
import top.mddata.open.admin.service.DocContentService;
import top.mddata.open.admin.service.DocGroupService;
import top.mddata.open.admin.service.DocInfoService;
import top.mddata.open.admin.vo.TornaModuleVo;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 文档分组 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocGroupServiceImpl extends SuperServiceImpl<DocGroupMapper, DocGroup> implements DocGroupService {
    private final TornaClient tornaClient;
    private final DocInfoService docInfoService;
    private final DocContentService docContentService;

    @Override
    public DocGroup saveDto(Object save) {
        DocGroup sopDocApp = super.saveBefore(save);

        TornaModuleVo tornaModuleDTO = tornaClient.execute("module.get", null, sopDocApp.getToken(), TornaModuleVo.class);
        DocGroup docApp = getOne(QueryWrapper.create().eq(DocGroup::getToken, sopDocApp.getToken()));
        if (docApp == null) {
            docApp = new DocGroup();
            docApp.setName(tornaModuleDTO.getName());
            docApp.setToken(sopDocApp.getToken());
            docApp.setPublish(BooleanEnum.TRUE.getInteger());
            save(docApp);
        } else {
            docApp.setPublish(BooleanEnum.TRUE.getInteger());
            docApp.setName(tornaModuleDTO.getName());
            updateById(docApp);
            sopDocApp.setId(docApp.getId());
        }
        // 同步文档
        docInfoService.syncDocInfo(docApp, null);
        return sopDocApp;
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        List<DocInfo> docList = docInfoService.list(QueryWrapper.create().in(DocInfo::getDocGroupId, idList));
        docInfoService.remove(QueryWrapper.create().in(DocInfo::getDocGroupId, idList));
        List<Long> docIdList = docList.stream().map(DocInfo::getId).toList();
        if (CollUtil.isNotEmpty(docIdList)) {
            docContentService.remove(QueryWrapper.create().in(DocContent::getDocInfoId, docIdList));
        }
        return super.removeByIds(idList);
    }
}
