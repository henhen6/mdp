package top.mddata.open.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import top.mddata.base.exception.BizException;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.base.utils.StrPool;
import top.mddata.common.constant.ConfigKey;
import top.mddata.common.enumeration.BooleanEnum;
import top.mddata.console.system.facade.ConfigFacade;
import top.mddata.open.admin.TreeNode;
import top.mddata.open.admin.entity.DocContent;
import top.mddata.open.admin.entity.DocGroup;
import top.mddata.open.admin.entity.DocInfo;
import top.mddata.open.admin.enumeration.DocSourceTypeEnum;
import top.mddata.open.admin.mapper.DocGroupMapper;
import top.mddata.open.admin.mapper.DocInfoMapper;
import top.mddata.open.admin.query.DocIdsParam;
import top.mddata.open.admin.service.DocContentService;
import top.mddata.open.admin.service.DocInfoService;
import top.mddata.open.admin.vo.DocInfoViewVo;
import top.mddata.open.admin.vo.DocInfoVo;
import top.mddata.open.admin.vo.DocSettingVo;
import top.mddata.open.admin.vo.TornaDocInfoViewVo;
import top.mddata.open.admin.vo.TornaDocInfoVo;
import top.mddata.open.admin.vo.TornaDocParamVo;
import top.mddata.open.admin.vo.TornaDocVo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文档信息 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocInfoServiceImpl extends SuperServiceImpl<DocInfoMapper, DocInfo> implements DocInfoService {
    private final DocGroupMapper docGroupMapper;
    private final DocContentService docContentService;
    private final TornaClient tornaClient;
    private final ConfigFacade configFacade;

    @Override
    @Transactional(readOnly = true)
    public List<DocInfoVo> tree(Long docGroupId, Integer isPublic) {
        List<DocInfo> list = list(QueryWrapper.create().eq(DocInfo::getDocGroupId, docGroupId).eq(DocInfo::getPublish, isPublic).orderBy(DocInfo::getWeight, true));
        List<DocInfoVo> voList = BeanUtil.copyToList(list, DocInfoVo.class);

        return MyTreeUtil.buildTreeEntity(voList, 0L, DocInfoVo::new);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updatePublish(Long id, Integer publish) {
        DocInfo docInfo = getById(id);
        ArgumentAssert.notNull(docInfo, "文档不存在");
        // 如果是文件夹,发布下面所有的文档
        boolean result;
        if (BooleanEnum.TRUE.eq(docInfo.getFolder())) {
            List<DocInfo> children = list(QueryWrapper.create().eq(DocInfo::getParentId, docInfo.getId()));
            Set<Long> ids = children.stream().map(DocInfo::getId).collect(Collectors.toSet());
            ids.add(id);
            DocInfo build = new DocInfo();
            build.setPublish(publish);
            result = update(build, QueryWrapper.create().in(DocInfo::getId, ids));
        } else {
            // 发布单个文档
            DocInfo build = UpdateEntity.of(DocInfo.class, docInfo.getId());
            build.setPublish(publish);
            result = updateById(build);

            // 发布父节点
            if (BooleanEnum.TRUE.eq(publish)) {
                DocInfo parent = new DocInfo();
                parent.setPublish(publish);
                parent.setId(docInfo.getParentId());
                updateById(parent);
            }
        }

        // 发布一个接口自动发布所属应用
        Long docGroupId = docInfo.getDocGroupId();
        if (BooleanEnum.TRUE.eq(publish)) {
            DocGroup docGroup = UpdateEntity.of(DocGroup.class, docGroupId);
            docGroup.setPublish(publish);
            docGroupMapper.update(docGroup);
        } else {
            // 如果应用下的接口都未发布,应用也改成未发布
            long count = count(QueryWrapper.create().eq(DocInfo::getDocGroupId, docGroupId).eq(DocInfo::getFolder, BooleanEnum.FALSE.getInteger()).eq(DocInfo::getPublish, BooleanEnum.TRUE.getInteger()));
            if (count == 0) {
                DocGroup docGroup = UpdateEntity.of(DocGroup.class, docGroupId);
                docGroup.setPublish(BooleanEnum.FALSE.getInteger());
                docGroupMapper.update(docGroup);
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncAllDoc(Long docAppId) {
        DocGroup docGroup = docGroupMapper.selectOneById(docAppId);
        this.syncDocInfo(docGroup, null);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDoc(Long docInfoId) {
        DocInfo docInfo = getById(docInfoId);
        DocGroup docGroup = docGroupMapper.selectOneById(docInfo.getDocGroupId());
        this.syncDocInfo(docGroup, docInfoId);
    }


    /**
     * 同步远程文档
     *
     * @param docApp    应用
     * @param docInfoId 同步某个文档,如果为null则同步整个应用文档
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDocInfo(DocGroup docApp, Long docInfoId) {
        Long docAppId = docApp.getId();
        List<DocInfo> docInfoList = list(QueryWrapper.create().eq(DocInfo::getDocGroupId, docAppId));
        Map<String, DocInfo> nameVersionMap = docInfoList.stream()
                .collect(Collectors.toMap(docInfo -> docInfo.getDocName() + StrPool.COLON + docInfo.getDocVersion(), Function.identity(), (v1, v2) -> v2));

        String token = docApp.getToken();
        // add doc
        DocIdsParam docIdsParam = buildSearchParam(docInfoId);
        TornaDocVo tornaDocDTO = tornaClient.execute("doc.list", docIdsParam, token, TornaDocVo.class);
        List<TornaDocInfoVo> docList = tornaDocDTO.getDocList();
        if (CollectionUtils.isEmpty(docList)) {
            return;
        }

        List<DocInfo> updateList = new ArrayList<>();
        for (TornaDocInfoVo tornaDocInfoDTO : docList) {
            String key = buildKey(tornaDocInfoDTO);
            DocInfo docInfo = nameVersionMap.get(key);
            // 需要修改的文档
            if (docInfo != null) {
                docInfo.setDocGroupId(docAppId);
                docInfo.setId(tornaDocInfoDTO.getId());
                docInfo.setDocTitle(tornaDocInfoDTO.getName());
                docInfo.setDocCode("");
                if (BooleanEnum.TRUE.eq(tornaDocInfoDTO.getIsFolder())) {
                    docInfo.setPublish(BooleanEnum.TRUE.getInteger());
                    docInfo.setDocName(tornaDocInfoDTO.getName());
                }
                docInfo.setDocType(tornaDocInfoDTO.getType().intValue());
                docInfo.setDescription(tornaDocInfoDTO.getDescription());
                docInfo.setFolder(tornaDocInfoDTO.getIsFolder());
                docInfo.setParentId(tornaDocInfoDTO.getParentId());
                docInfo.setSourceType(DocSourceTypeEnum.TORNA.getCode());
                updateList.add(docInfo);
            }
            super.updateBatch(updateList);
        }

        // 新增的文档
        List<DocInfo> saveList = docList.stream()
                .filter(tornaDocInfoDTO -> {
                    String key = buildKey(tornaDocInfoDTO);
                    return !nameVersionMap.containsKey(key);
                })
                .map(tornaDocInfoDTO -> {
                    DocInfo docInfo = new DocInfo();
                    docInfo.setDocGroupId(docAppId);
                    docInfo.setId(tornaDocInfoDTO.getId());
                    docInfo.setDocTitle(tornaDocInfoDTO.getName());
                    docInfo.setDocCode("");
                    docInfo.setDocType(tornaDocInfoDTO.getType().intValue());
                    docInfo.setSourceType(DocSourceTypeEnum.TORNA.getCode());
                    if (BooleanEnum.TRUE.eq(tornaDocInfoDTO.getIsFolder())) {
                        docInfo.setPublish(BooleanEnum.TRUE.getInteger());
                        docInfo.setDocName(tornaDocInfoDTO.getName());
                    } else {
                        docInfo.setPublish(BooleanEnum.FALSE.getInteger());
                        docInfo.setDocName(tornaDocInfoDTO.getUrl());
                    }
                    docInfo.setDocVersion(tornaDocInfoDTO.getVersion());
                    docInfo.setDescription(tornaDocInfoDTO.getDescription());
                    docInfo.setFolder(tornaDocInfoDTO.getIsFolder());
                    docInfo.setParentId(tornaDocInfoDTO.getParentId());
                    return docInfo;
                })
                .collect(Collectors.toList());
        saveBatch(saveList);

        Set<Long> docIds = docList.stream().map(TornaDocInfoVo::getId).collect(Collectors.toSet());
        this.syncContent(docApp, docIds);
    }

    @Override
    public DocInfoViewVo getDocView(Long id) {
        TornaDocInfoViewVo tornaDocInfoViewDTO = getDocDetail(id);
        DocSettingVo docInfoConfigVo = getDocSetting();

        DocInfoViewVo docInfoViewDTO = new DocInfoViewVo();
        docInfoViewDTO.setDocInfoView(tornaDocInfoViewDTO);
        docInfoViewDTO.setDocInfoConfig(docInfoConfigVo);
        return docInfoViewDTO;
    }

    private DocSettingVo getDocSetting() {
        DocSettingVo docSettingDTO = new DocSettingVo();
        docSettingDTO.setTornaServerAddr(configFacade.getString(ConfigKey.Open.TORNA_SERVER_ADDR, "未配置"));
        docSettingDTO.setOpenProdUrl(configFacade.getString(ConfigKey.Open.OPEN_PROD_URL, "未配置"));
        docSettingDTO.setOpenSandboxUrl(configFacade.getString(ConfigKey.Open.OPEN_SANDBOX_URL, "未配置"));
        return docSettingDTO;
    }

    private TornaDocInfoViewVo getDocDetail(Long id) {
        DocInfo docInfo = this.getById(id);
        if (docInfo == null || !BooleanEnum.TRUE.eq(docInfo.getPublish())) {
            throw new BizException("文档不存在");
        }

        DocContent sopDocContent = docContentService.getOne(QueryWrapper.create().eq(DocContent::getDocInfoId, docInfo.getId()));
        if (sopDocContent == null) {
            return new TornaDocInfoViewVo();
        }

        return JSON.parseObject(sopDocContent.getContent(), TornaDocInfoViewVo.class);
    }

    private void syncContent(DocGroup docApp, Set<Long> docIds) {
        List<DocInfo> list = list(QueryWrapper.create().eq(DocInfo::getDocGroupId, docApp.getId()).in(DocInfo::getId, docIds));
        Map<Long, String> docIdMap = this.getContentMap(docApp.getToken(), docIds);
        for (DocInfo docInfo : list) {
            String content = docIdMap.getOrDefault(docInfo.getId(), "");
            docContentService.saveContent(docInfo.getId(), content);
        }
    }

    /**
     * 批量获取Torna文档内容
     *
     * @param token  token
     * @param docIds Torna文档id
     * @return key:文档id, value:文档内容
     */
    private Map<Long, String> getContentMap(String token, Collection<Long> docIds) {
        // 获取torna文档信息
        List<TornaDocInfoViewVo> tornaDocInfoViewList = tornaClient.executeList(
                "doc.details",
                new DocIdsParam(docIds),
                token,
                TornaDocInfoViewVo.class
        );
        for (TornaDocInfoViewVo docInfoViewDTO : tornaDocInfoViewList) {
            convertTree(docInfoViewDTO);
        }
        return tornaDocInfoViewList.stream()
                .collect(Collectors.toMap(TornaDocInfoViewVo::getId, JSON::toJSONString, (v1, v2) -> v1));
    }

    private void convertTree(TornaDocInfoViewVo tornaDocInfoViewDTO) {
        List<TornaDocParamVo> requestParams = tornaDocInfoViewDTO.getRequestParams();
        List<TornaDocParamVo> responseParams = tornaDocInfoViewDTO.getResponseParams();
        List<TornaDocParamVo> requestTree = TreeNode.convertTree(requestParams, 0L);
        List<TornaDocParamVo> responseTree = TreeNode.convertTree(responseParams, 0L);

        tornaDocInfoViewDTO.setRequestParams(requestTree);
        tornaDocInfoViewDTO.setResponseParams(responseTree);
    }

    private String buildKey(TornaDocInfoVo tornaDocInfoDTO) {
        return BooleanEnum.TRUE.eq(tornaDocInfoDTO.getIsFolder()) ?
                tornaDocInfoDTO.getName() + ":" + tornaDocInfoDTO.getVersion()
                : tornaDocInfoDTO.getUrl() + ":" + tornaDocInfoDTO.getVersion();
    }

    private DocIdsParam buildSearchParam(Long docInfoId) {
        if (docInfoId == null) {
            return null;
        }
        DocIdsParam docIdsParam = new DocIdsParam();
        DocInfo docInfo = getById(docInfoId);
        List<Long> docIdList = new ArrayList<>();
        docIdList.add(docInfo.getId());
        // 如果是文件夹,找下面的子文档
        if (BooleanEnum.TRUE.eq(docInfo.getFolder())) {
            List<DocInfo> docList = list(QueryWrapper.create().eq(DocInfo::getParentId, docInfo.getId()));
            List<Long> docIds = docList.stream().map(DocInfo::getId).toList();
            docIdList.addAll(docIds);
        }
        docIdsParam.setDocIds(docIdList);
        return docIdsParam;
    }
}
