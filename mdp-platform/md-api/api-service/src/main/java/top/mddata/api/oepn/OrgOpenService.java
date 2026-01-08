package top.mddata.api.oepn;

import com.gitee.sop.support.annotation.Open;
import com.mybatisflex.core.paginate.Page;
import top.mddata.api.oepn.dto.OrgSaveDto;
import top.mddata.api.oepn.dto.OrgUpdateDto;
import top.mddata.api.oepn.query.OrgQuery;
import top.mddata.api.oepn.resp.OrgResp;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.common.entity.Org;

/**
 * 组织数据接口
 * @author henhen
 * @since 2026/1/7 11:14
 */
public interface OrgOpenService extends SuperService<Org> {
    /**
     * 批量保存组织机构信息
     *
     * @param dto 组织机构信息
     * @return 保存成功后的组织机构信息
     */
    @Open("org.save")
    OrgResp save(OrgSaveDto dto);


    /**
     * 根据组织机构ID，修改组织机构信息
     *
     * @param dto 组织机构信息
     * @return 修改成功后的组织机构信息
     */
    @Open("org.updateById")
    OrgResp updateById(OrgUpdateDto dto);

    /**
     * 根据ID查询组织机构信息
     *
     * @param id 组织机构ID
     * @return 组织机构信息
     */
    @Open("org.getById")
    OrgResp getVoById(Long id);

    /**
     * 分页查询组织机构信息
     *
     * @param params 分页查询参数
     * @return 分页数据
     */
    @Open("org.page")
    Page<OrgResp> page(PageParams<OrgQuery> params);
}
