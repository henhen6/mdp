package top.mddata.open.admin.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.admin.entity.DocGroup;
import top.mddata.open.admin.entity.DocInfo;
import top.mddata.open.admin.vo.DocInfoViewVo;
import top.mddata.open.admin.vo.DocInfoVo;

import java.util.List;

/**
 * 文档信息 服务层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
public interface DocInfoService extends SuperService<DocInfo> {
    /**
     * 根据文档分组查询树结构的文档信息
     *
     * @param docGroupId 文档分组id
     * @param isPublic   状态
     * @return 文档树结构
     */
    List<DocInfoVo> tree(Long docGroupId, Integer isPublic);

    /**
     * 修改发布状态
     *
     * @param id        id
     * @param isPublish 状态
     * @return 是否成功
     */
    Boolean updatePublish(Long id, Integer isPublish);

    /**
     * 同步文档
     *
     * @param docGroupId 文档分组id
     */
    void syncAllDoc(Long docGroupId);

    /**
     * 同步文档
     *
     * @param docInfoId 文档id
     */
    void syncDoc(Long docInfoId);

    /**
     * 同步文档信息
     * @param docGroup 文档分组
     * @param docInfoId 文档id
     */
    void syncDocInfo(DocGroup docGroup, Long docInfoId);

    /**
     * 获取文档详情
     * @param id 文档id
     * @return 文档详情
     */
    DocInfoViewVo getDocView(Long id);
}
