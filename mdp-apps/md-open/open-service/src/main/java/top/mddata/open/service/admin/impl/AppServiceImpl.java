package top.mddata.open.service.admin.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.mybatisflex.utils.BeanPageUtil;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.util.StrPool;
import top.mddata.common.cache.open.AppByAppKeyCkBuilder;
import top.mddata.common.cache.open.AppCkBuilder;
import top.mddata.common.constant.FileObjectType;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.console.dto.system.RelateFilesToBizDto;
import top.mddata.console.entity.permission.Role;
import top.mddata.console.entity.permission.RoleAppRel;
import top.mddata.console.facade.system.FileFacade;
import top.mddata.open.dto.admin.AppDto;
import top.mddata.open.dto.client.AppDevInfoDto;
import top.mddata.open.dto.client.AppInfoUpdateDto;
import top.mddata.open.entity.admin.App;
import top.mddata.open.entity.admin.AppKeys;
import top.mddata.open.mapper.admin.AppKeysMapper;
import top.mddata.open.mapper.admin.AppMapper;
import top.mddata.open.query.admin.AppQuery;
import top.mddata.open.service.admin.AppService;
import top.mddata.open.vo.admin.AppVo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 应用 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppServiceImpl extends SuperServiceImpl<AppMapper, App> implements AppService {
    private final FileFacade fileFacade;
    private final UidGenerator uidGenerator;
    private final AppKeysMapper appKeysMapper;

    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new AppCkBuilder();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppVo> pageByRoleTemplateId(Page<App> page, AppQuery query) {
        Map<String, Object> otherParams = new HashMap<>();
        otherParams.put("state", query.getState());
        otherParams.put("name", query.getName());
        otherParams.put("appKey", query.getAppKey());
        otherParams.put("loginType", query.getLoginType());
        otherParams.put("type", query.getType());
        otherParams.put("roleId", query.getRoleId());
        otherParams.put("hasApp", query.getHasApp() != null && query.getHasApp());

        Page<App> pageResult = mapper.xmlPaginate("pageByRoleId", page, otherParams);
        return BeanPageUtil.toBeanPage(pageResult, AppVo.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppVo> pageByRoleId(Page<App> page, AppQuery query) {
        Map<String, Object> otherParams = new HashMap<>();
        otherParams.put("state", query.getState());
        otherParams.put("name", query.getName());
        otherParams.put("appKey", query.getAppKey());
        otherParams.put("loginType", query.getLoginType());
        otherParams.put("type", query.getType());
        otherParams.put("roleId", query.getRoleId());
        // 当前组织性质下的、权限集合角色
        otherParams.put("roleCategory", RoleCategoryEnum.PERM_SET.getCode());
        Integer currentCompanyNature = ContextUtil.getCurrentCompanyNature();
        otherParams.put("orgNature", currentCompanyNature == null ? OrgNatureEnum.DEFAULT.getCode() : currentCompanyNature);
        otherParams.put("templateRole", 1);
        otherParams.put("hasAppByRole", query.getHasApp() != null && query.getHasApp());

        Page<App> pageResult = mapper.xmlPaginate("pageByRoleId", page, otherParams);

        return BeanPageUtil.toBeanPage(pageResult, AppVo.class);
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppVo> listNeedPushApp() {
        return listAs(QueryWrapper.create().eq(App::getSsoPush, true), AppVo.class);
    }

    @Override
    @Transactional(readOnly = true)
    public AppVo getAppByAppKey(String appKey) {
        CacheKey idKey = AppByAppKeyCkBuilder.builder(appKey);
        CacheResult<Long> appIdCache = cacheOps.get(idKey, (k) -> {
            App app = mapper.selectOneByQuery(QueryWrapper.create().eq(App::getAppKey, appKey));
            return app != null ? app.getId() : null;
        });

        if (appIdCache.isNullVal() || appIdCache.getValue() == null) {
            return null;
        }
        Long appId = appIdCache.asLong();

        CacheKey entityKey = AppCkBuilder.builder(appId);
        CacheResult<App> apiCache = cacheOps.get(entityKey, (k) -> mapper.selectOneById(appId));

        return BeanUtil.toBean(apiCache.getValue(), AppVo.class);
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppVo> listAppByUserId(Long userId) {
        Iterable<QueryColumn> queryColumns = QueryMethods.defaultColumns(App.class);
        // 将过滤条件移到 on 中，可以减少 join 数据量，提高查询效率
        QueryWrapper queryWrapper = QueryWrapper.create().select(queryColumns).from(App.class)
                .innerJoin(RoleAppRel.class).on(RoleAppRel::getAppId, App::getId)
                .innerJoin(Role.class).on(RoleAppRel::getRoleId, Role::getId).eq(Role::getState, true)
                .innerJoin(UserRoleRel.class).on(Role::getId, UserRoleRel::getRoleId).eq(UserRoleRel::getUserId, userId)
                .where(App::getShow).eq(true)
                .eq(App::getState, true)
                .orderBy(App::getWeight, true);
        return listAs(queryWrapper, AppVo.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkAppByUserId(Long userId, Long appId) {
        Iterable<QueryColumn> queryColumns = QueryMethods.defaultColumns(App.class);
        // 将过滤条件移到 on 中，可以减少 join 数据量，提高查询效率
        QueryWrapper queryWrapper = QueryWrapper.create().select(queryColumns).from(App.class)
                .innerJoin(RoleAppRel.class).on(RoleAppRel::getAppId, App::getId)
                .innerJoin(Role.class).on(RoleAppRel::getRoleId, Role::getId).eq(Role::getState, true)
                .innerJoin(UserRoleRel.class).on(Role::getId, UserRoleRel::getRoleId).eq(UserRoleRel::getUserId, userId)
                .where(App::getShow).eq(true)
                .eq(App::getState, true)
                .eq(App::getId, appId)
                .orderBy(App::getWeight, true);
        return count(queryWrapper) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppVo> listMyApp(Long userId) {
        List<AppVo> list = listAppByUserId(userId);

        QueryWrapper wrapper = QueryWrapper.create()
                .eq(App::getShow, true)
                .eq(App::getState, true)
                .eq(App::getIsPublic, true) // 公开应用
                .orderBy(App::getWeight, true);
        List<AppVo> publicList = listAs(wrapper, AppVo.class);

        // 根据 id 合并2个list,并根据weight升序排序
        return Stream.concat(list.stream(), publicList.stream())
                // 过滤id为null的元素，避免空指针
                .filter(app -> app.getId() != null)
                // 转Map去重：key=id，重复时保留第一个（list中的元素）
                .collect(Collectors.toMap(
                        AppVo::getId,   // 以id作为唯一key
                        app -> app,     // 以AppVo本身为value
                        (existing, newApp) -> existing // 重复时保留已存在的（list优先）
                ))
                // 取出Map的values并转为流
                .values().stream()
                // 按weight升序排序（处理weight为null的情况）
                .sorted(Comparator.comparing(AppVo::getWeight,
                        // 自定义null值处理：null排最前，非null按默认升序
                        Comparator.nullsFirst(Integer::compareTo)
                ).reversed())  // 实现降序
                // 转为最终List
                .collect(Collectors.toList());
    }

    @Override
    protected App saveBefore(Object save) {
        AppDto dto = (AppDto) save;
        App opApplication = BeanUtil.toBean(dto, App.class, CopyOptions.create().setIgnoreProperties("oauth2AllowGrantTypes"));
        opApplication.setId(uidGenerator.getUid());
        String appKey = new SimpleDateFormat("yyyyMMdd").format(new Date()) + RandomUtil.randomString(10);
        opApplication.setAppKey(appKey);
        opApplication.setLogo(opApplication.getId());
        opApplication.setAppSecret(RandomUtil.randomString(36));
        opApplication.setOauth2AllowGrantTypes(StrUtil.join(StrPool.COMMA, dto.getOauth2AllowGrantTypes()));
        return opApplication;
    }

    @Override
    protected App updateBefore(Object updateDto) {
        AppDto dto = (AppDto) updateDto;
        App opApplication = UpdateEntity.of(getEntityClass());
        BeanUtil.copyProperties(updateDto, opApplication);
        opApplication.setLogo(opApplication.getId());
        opApplication.setOauth2AllowGrantTypes(StrUtil.join(StrPool.COMMA, dto.getOauth2AllowGrantTypes()));
        return opApplication;
    }

    @Override
    protected void saveAfter(Object save, App entity) {
        AppDto dto = (AppDto) save;

        AppKeys appKeys = new AppKeys();
        appKeys.setAppId(entity.getId());
        appKeysMapper.insert(appKeys);

//        关联LOGO
        fileFacade.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getId())
                .objectType(FileObjectType.Open.APP_LOGO)
                .build().setKeepFileIds(dto.getLogoFileId()));

        cacheOps.del(AppByAppKeyCkBuilder.builder(entity.getAppKey()));
    }

    @Override
    protected void updateAfter(Object updateDto, App entity) {
        AppDto dto = (AppDto) updateDto;

//        关联LOGO
        fileFacade.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getId())
                .objectType(FileObjectType.Open.APP_LOGO)
                .build().setKeepFileIds(dto.getLogoFileId()));

        cacheOps.del(AppByAppKeyCkBuilder.builder(entity.getAppKey()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateInfoById(AppInfoUpdateDto dto) {
        App entity = UpdateEntity.of(getEntityClass());
        BeanUtil.copyProperties(dto, entity);
        entity.setLogo(entity.getId());

        updateById(entity);

//        关联LOGO
        fileFacade.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getId())
                .objectType(FileObjectType.Open.APP_LOGO)
                .build().setKeepFileIds(dto.getLogoFileId()));

        cacheOps.del(AppByAppKeyCkBuilder.builder(entity.getAppKey()));
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateDevById(AppDevInfoDto dto) {
        App entity = UpdateEntity.of(getEntityClass());
        BeanUtil.copyProperties(dto, entity);
        updateById(entity);

        cacheOps.del(AppByAppKeyCkBuilder.builder(entity.getAppKey()));
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        List<App> apps = mapper.selectListByIds(idList);
        boolean flag = super.removeByIds(idList);

        List<CacheKey> cacheKeys = apps.stream().map(App::getAppKey).map(AppByAppKeyCkBuilder::builder).toList();
        cacheOps.del(cacheKeys);
        return flag;
    }
}
