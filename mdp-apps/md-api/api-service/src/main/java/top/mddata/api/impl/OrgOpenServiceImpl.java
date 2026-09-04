package top.mddata.api.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.gitee.sop.support.context.OpenContext;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import top.mddata.api.OrgOpenService;
import top.mddata.api.dto.OrgSaveDto;
import top.mddata.api.dto.OrgUpdateDto;
import top.mddata.api.query.OrgQuery;
import top.mddata.api.resp.OrgResp;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.base.mybatisflex.utils.BeanPageUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.common.cache.console.organization.OrgCacheKeyBuilder;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.OrgNature;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.organization.OrgTypeEnum;
import top.mddata.common.mapper.OrgMapper;
import top.mddata.common.mapper.OrgNatureMapper;
import top.mddata.open.dto.admin.NotifyInfoDto;
import top.mddata.open.facade.admin.NotifyAndEventPushFacade;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author henhen
 * @since 2026/1/7 12:25
 */
@DubboService
@Service
@Slf4j
@RequiredArgsConstructor
public class OrgOpenServiceImpl extends SuperServiceImpl<OrgMapper, Org> implements OrgOpenService {
    private final UidGenerator uidGenerator;
    private final OrgNatureMapper orgNatureMapper;
    private final NotifyAndEventPushFacade notifyAndEventPushFacade;

    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new OrgCacheKeyBuilder();
    }

    @Override
    public OrgResp save(OrgSaveDto dto) {
        Org entity = BeanUtil.toBean(dto, Org.class);
        entity.setId(null);

        Org parent = null;
        if (entity.getParentId() != null) {
            parent = super.getById(entity.getParentId());
            ArgumentAssert.notNull(parent, "上级节点不能为空");

            if (OrgTypeEnum.COMPANY.eq(entity.getOrgType())) {
                ArgumentAssert.isFalse(OrgTypeEnum.DEPT.eq(parent.getOrgType()), "{}不能挂在{}下", OrgTypeEnum.COMPANY.getDesc(), OrgTypeEnum.DEPT.getDesc());
            }
        }

        entity.setId(uidGenerator.getUid());
        fill(entity, parent);

        save(entity);

        OrgNature orgNature = new OrgNature();
        orgNature.setNature(OrgNatureEnum.DEFAULT.getCode());
        orgNature.setOrgId(entity.getId());
        orgNatureMapper.insert(orgNature);

        delCache(entity);

        // 这里只是做一个演示代码，通常只有处理比较耗时的逻辑时，才需要异步回调。
        callNotify(entity);

        return BeanUtil.toBean(entity, OrgResp.class);
    }

    private void callNotify(Org org) {
        OpenContext openContext = OpenContext.current();
        NotifyInfoDto notifyRequest = new NotifyInfoDto();
        notifyRequest.setCallLogId(openContext.getCallLogId());
        notifyRequest.setAppId(openContext.getAppId());
        notifyRequest.setAppKey(openContext.getAppKey());
        notifyRequest.setApiName(openContext.getApiName());
        notifyRequest.setApiVersion(openContext.getVersion());
        notifyRequest.setClientIp(openContext.getClientIp());
        notifyRequest.setNotifyUrl(openContext.getNotifyUrl());
        notifyRequest.setCharset(openContext.getCharset());
        // 模拟传递需要回调的参数
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("id", org.getId());

        notifyRequest.setBizParams(bizParams);
        notifyRequest.setRemark("新增组织回调");

        notifyAndEventPushFacade.apiCallNotify(notifyRequest);
    }

    private void fill(Org item, Org parent) {
        if (parent == null) {
            item.setParentId(MyTreeUtil.DEF_PARENT_ID);
            item.setTreePath(MyTreeUtil.buildTreePath(item.getId()));
        } else {
            item.setParentId(parent.getId());
            item.setTreePath(MyTreeUtil.buildTreePath(parent.getTreePath(), item.getId()));
        }
    }

    @Override
    public OrgResp updateById(OrgUpdateDto data) {
        Org org = super.updateBefore(data);

        Org parent = null;
        if (data.getParentId() != null) {
            parent = getById(data.getParentId());
            ArgumentAssert.notNull(parent, "上级节点不能为空");

            if (OrgTypeEnum.COMPANY.eq(org.getOrgType())) {
                ArgumentAssert.isFalse(OrgTypeEnum.DEPT.eq(parent.getOrgType()), "{}不能挂在{}下", OrgTypeEnum.COMPANY.getDesc(), OrgTypeEnum.DEPT.getDesc());
            }
        }

        fill(org, parent);
        updateById(org);
        delCache(org);
        return BeanUtil.toBean(org, OrgResp.class);
    }

    @Override
    public OrgResp getVoById(Long id) {
        return BeanUtil.toBean(getById(id), OrgResp.class);
    }

    @Override
    public Page<OrgResp> page(PageParams<OrgQuery> params) {
        Page<Org> page = Page.of(params.getCurrent(), params.getSize());
        Org entity = BeanUtil.toBean(params.getModel(), Org.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        mapper.paginate(page, wrapper);
        return BeanPageUtil.toBeanPage(page, OrgResp.class);
    }
}
