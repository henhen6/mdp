package top.mddata.console.service.permission.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.console.permission.ResourceApiAllCacheKeyBuilder;
import top.mddata.console.dto.permission.ResourceApiBindDto;
import top.mddata.console.dto.permission.ResourceApiSaveDto;
import top.mddata.console.entity.permission.ResourceApi;
import top.mddata.console.entity.permission.RoleResourceRel;
import top.mddata.console.mapper.permission.ResourceApiMapper;
import top.mddata.console.mapper.permission.RoleResourceRelMapper;
import top.mddata.console.service.permission.ResourceApiService;
import top.mddata.console.service.permission.RoleResourceRelService;
import top.mddata.console.vo.permission.ResourceApiVo;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 接口权限 服务层实现。
 *
 * @author henhen6
 * @since 2026-09-27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceApiServiceImpl
        extends SuperServiceImpl<ResourceApiMapper, ResourceApi>
        implements ResourceApiService {
    private final RoleResourceRelMapper roleResourceRelMapper;
    private final RoleResourceRelService roleResourceRelService;

    @Override
    @Transactional(readOnly = true)
    public List<ResourceApiVo> listByResource(Long resourceId) {
        return listAs(QueryWrapper.create()
                .where(ResourceApi::getResourceId).eq(resourceId), ResourceApiVo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveManual(ResourceApiSaveDto dto) {
        ArgumentAssert.isTrue(dto.getUri().startsWith("/"), "接口路径必须以 / 开头");
        ResourceApi entity = BeanUtil.toBean(dto, ResourceApi.class);
        entity.setIsInput(Boolean.TRUE);
        // uk 幂等：同资源下同接口已存在则视为成功（前端重复提交/扫描撞车）
        long count = count(QueryWrapper.create()
                .where(ResourceApi::getResourceId).eq(dto.getResourceId())
                .and(ResourceApi::getUri).eq(dto.getUri())
                .and(ResourceApi::getRequestMethod).eq(dto.getRequestMethod()));
        if (count == 0) {
            save(entity);
        }
        invalidateCache(dto.getResourceId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer bind(ResourceApiBindDto dto) {
        List<ResourceApi> existed = list(QueryWrapper.create()
                .where(ResourceApi::getResourceId).eq(dto.getResourceId()));
        List<ResourceApiBindDto.ApiItem> freshList = filterNewApis(existed, dto.getApiList());
        if (!freshList.isEmpty()) {
            List<ResourceApi> entities = freshList.stream().map(item -> {
                ResourceApi e = BeanUtil.toBean(item, ResourceApi.class);
                e.setResourceId(dto.getResourceId());
                e.setIsInput(Boolean.FALSE);
                return e;
            }).toList();
            saveBatch(entities);
            invalidateCache(dto.getResourceId());
        }
        return freshList.size();
    }

    /**
     * 过滤出未关联的新接口（纯函数）：按 uri+requestMethod 判重，含入参自身去重。
     */
    public static List<ResourceApiBindDto.ApiItem> filterNewApis(List<ResourceApi> existed,
                                                                 List<ResourceApiBindDto.ApiItem> apiList) {
        Set<String> seen = existed.stream()
                .map(e -> key(e.getUri(), e.getRequestMethod()))
                .collect(Collectors.toCollection(HashSet::new));
        return apiList.stream()
                .filter(item -> seen.add(key(item.getUri(), item.getRequestMethod())))
                .toList();
    }

    private static String key(String uri, String method) {
        return uri + "###" + method;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<Long> ids) {
        List<ResourceApi> list = listByIds(ids);
        removeByIds(ids);
        // 受影响资源可能不同，逐个失效
        list.forEach(e -> invalidateCache(e.getResourceId()));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByResource(Collection<? extends Serializable> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return false;
        }
        List<ResourceApi> list = list(QueryWrapper.create().in(ResourceApi::getResourceId, resourceIds));
        if (list.isEmpty()) {
            return false;
        }
        remove(QueryWrapper.create().in(ResourceApi::getResourceId, resourceIds));
        // 受影响资源可能多个，按资源维度去重后逐个失效
        list.stream()
                .collect(Collectors.toMap(
                        ResourceApi::getResourceId,
                        e -> e,
                        (a, b) -> a))
                .values()
                .forEach(e -> invalidateCache(e.getResourceId()));
        return true;
    }

    /**
     * 配置变更失效：全量缓存A + 引用该资源的角色下所有用户的缓存B。
     * 授权链：mdc_role_resource_rel → mdc_user_role_rel，两步查询（不 join）。
     */
    private void invalidateCache(Long resourceId) {
        cacheOps.del(ResourceApiAllCacheKeyBuilder.build());
        List<Long> roleIds = roleResourceRelMapper.selectListByQuery(QueryWrapper.create()
                        .select(RoleResourceRel::getRoleId)
                        .where(RoleResourceRel::getResourceId).eq(resourceId))
                .stream().map(RoleResourceRel::getRoleId).distinct().toList();
        if (!roleIds.isEmpty()) {
            roleResourceRelService.invalidateUserResourceApiCacheByRoleIds(roleIds);
        }
    }
}
