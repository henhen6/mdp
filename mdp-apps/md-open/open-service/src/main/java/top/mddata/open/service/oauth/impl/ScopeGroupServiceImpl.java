package top.mddata.open.service.oauth.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.open.AppApiCkBuilder;
import top.mddata.common.enumeration.AuditStatusEnum;
import top.mddata.open.entity.admin.Api;
import top.mddata.open.entity.admin.AppGroupRel;
import top.mddata.open.entity.admin.GroupApiRel;
import top.mddata.open.entity.admin.GroupScopeRel;
import top.mddata.open.entity.admin.OauthScope;
import top.mddata.open.entity.admin.ScopeGroup;
import top.mddata.open.mapper.admin.ApiMapper;
import top.mddata.open.mapper.admin.AppGroupRelMapper;
import top.mddata.open.mapper.admin.GroupApiRelMapper;
import top.mddata.open.mapper.admin.OauthScopeMapper;
import top.mddata.open.mapper.admin.ScopeGroupMapper;
import top.mddata.open.service.oauth.ScopeGroupService;
import top.mddata.open.vo.admin.ApiVo;
import top.mddata.open.vo.admin.OauthScopeVo;
import top.mddata.open.vo.admin.ScopeGroupDetailVo;
import top.mddata.open.vo.admin.ScopeGroupVo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 应用权限分组 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScopeGroupServiceImpl extends SuperServiceImpl<ScopeGroupMapper, ScopeGroup> implements ScopeGroupService {
    private final AppGroupRelMapper appGroupRelMapper;
    private final GroupApiRelMapper groupApiRelMapper;
    private final ApiMapper apiMapper;
    private final OauthScopeMapper oauthScopeMapper;

    @Override
    protected void updateAfter(Object update, ScopeGroup entity) {
        delAppApiCache(Collections.singletonList(entity.getId()));
    }

    private void delAppApiCache(Collection<? extends Serializable> idList) {
        // 查询分组下的应用
        List<AppGroupRel> appGroupList = appGroupRelMapper.selectListByQuery(QueryWrapper.create().in(AppGroupRel::getGroupId, idList));
        if (CollUtil.isNotEmpty(appGroupList)) {
            // 清理应用拥有的开放接口
            List<CacheKey> keys = appGroupList.stream().map(AppGroupRel::getAppId).distinct().map(AppApiCkBuilder::builder).toList();
            cacheOps.del(keys);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        delAppApiCache(Collections.singletonList(id));
        return removeByIds(Collections.singletonList(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(ScopeGroup entity) {
        delAppApiCache(Collections.singletonList(entity.getId()));
        return removeByIds(Collections.singletonList(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        delAppApiCache(idList);

        boolean flag = super.removeByIds(idList);
        appGroupRelMapper.deleteByQuery(QueryWrapper.create().in(AppGroupRel::getGroupId, idList));
        groupApiRelMapper.deleteByQuery(QueryWrapper.create().in(GroupApiRel::getGroupId, idList));
        delCache(idList);
        return flag;
    }

    @Override
    public List<ScopeGroupVo> listAppliable(Long appId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(QueryMethods.defaultColumns(ScopeGroup.class))
                .from(ScopeGroup.class)
                .where(ScopeGroup::getState).eq(true)
                // 排除已拥有
                .and("not exists (select 1 from mdo_app_group_rel rel where rel.group_id = mdo_scope_group.id and rel.app_id = ?)", appId)
                // 排除在审申请单
                .and("not exists (select 1 from mdo_app_scope_apply apply where apply.group_id = mdo_scope_group.id and apply.app_id = ? and apply.audit_status = ?)",
                        appId, AuditStatusEnum.PENDING.getCode())
                .orderBy(ScopeGroup::getWeight, true);
        return listAs(wrapper, ScopeGroupVo.class);
    }

    /**
     * 查询应用已拥有的权限组。
     * 重复防护由 mdo_app_group_rel 唯一索引 uk_app_id_group_id 保证，无需 distinct。
     */
    @Override
    public List<ScopeGroupVo> listOwned(Long appId) {
        QueryWrapper wrapper = QueryWrapper.create()
                .select(QueryMethods.defaultColumns(ScopeGroup.class))
                .from(ScopeGroup.class)
                .innerJoin(AppGroupRel.class).on(AppGroupRel::getGroupId, ScopeGroup::getId)
                .where(AppGroupRel::getAppId).eq(appId)
                .orderBy(ScopeGroup::getWeight, true);
        return listAs(wrapper, ScopeGroupVo.class);
    }

    @Override
    public ScopeGroupDetailVo getGroupDetail(Long groupId) {
        ScopeGroup group = getById(groupId);
        ArgumentAssert.notNull(group, "权限组不存在");
        ScopeGroupDetailVo detail = BeanUtil.toBean(group, ScopeGroupDetailVo.class);

        // 组内开放接口（mdo_group_api_rel -> mdo_api）
        QueryWrapper apiWrapper = QueryWrapper.create()
                .select(QueryMethods.defaultColumns(Api.class))
                .from(Api.class)
                .innerJoin(GroupApiRel.class).on(GroupApiRel::getApiId, Api::getId)
                .where(GroupApiRel::getGroupId).eq(groupId);
        detail.setApis(apiMapper.selectListByQueryAs(apiWrapper, ApiVo.class));

        // 组内 oauth2 权限（mdo_group_scope_rel -> mdo_oauth_scope）
        QueryWrapper scopeWrapper = QueryWrapper.create()
                .select(QueryMethods.defaultColumns(OauthScope.class))
                .from(OauthScope.class)
                .innerJoin(GroupScopeRel.class).on(GroupScopeRel::getScopeId, OauthScope::getId)
                .where(GroupScopeRel::getGroupId).eq(groupId);
        detail.setScopes(oauthScopeMapper.selectListByQueryAs(scopeWrapper, OauthScopeVo.class));
        return detail;
    }
}
