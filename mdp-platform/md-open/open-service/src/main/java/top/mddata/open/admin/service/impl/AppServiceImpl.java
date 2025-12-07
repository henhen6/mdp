package top.mddata.open.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.gitee.sop.support.util.StrPool;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.mybatisflex.utils.BeanPageUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.ContextUtil;
import top.mddata.common.constant.FileObjectType;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.permission.RoleCategoryEnum;
import top.mddata.console.system.dto.RelateFilesToBizDto;
import top.mddata.console.system.facade.FileFacade;
import top.mddata.open.admin.dto.AppDto;
import top.mddata.open.admin.dto.AppKeysDto;
import top.mddata.open.admin.entity.App;
import top.mddata.open.admin.entity.AppKeys;
import top.mddata.open.admin.mapper.AppMapper;
import top.mddata.open.admin.query.AppQuery;
import top.mddata.open.admin.service.AppKeysService;
import top.mddata.open.admin.service.AppService;
import top.mddata.open.admin.utils.RsaTool;
import top.mddata.open.admin.vo.AppKeysVo;
import top.mddata.open.admin.vo.AppVo;
import top.mddata.open.client.dto.AppDevInfoDto;
import top.mddata.open.client.dto.AppInfoUpdateDto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final AppKeysService appKeysService;

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
        otherParams.put("orgNature", ContextUtil.getCurrentCompanyNature());
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
        return getOneAs(QueryWrapper.create().eq(App::getAppKey, appKey), AppVo.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppVo> listMyApp(Long userId) {
//        TODO henhen6
        return listAs(QueryWrapper.create(), AppVo.class);
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

//        关联LOGO
        fileFacade.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getId())
                .objectType(FileObjectType.Open.APP_LOGO)
                .build().setKeepFileIds(dto.getLogo()));
    }

    @Override
    protected void updateAfter(Object updateDto, App entity) {
        AppDto dto = (AppDto) updateDto;

//        关联LOGO
        fileFacade.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getId())
                .objectType(FileObjectType.Open.APP_LOGO)
                .build().setKeepFileIds(dto.getLogo()));
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
                .build().setKeepFileIds(dto.getLogo()));
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateDevById(AppDevInfoDto dto) {
        App entity = UpdateEntity.of(getEntityClass());
        BeanUtil.copyProperties(dto, entity);

        updateById(entity);
        return entity.getId();
    }

    @Override
    public RsaTool.KeyStore createKeys(Integer keyFormat) throws Exception {
        RsaTool.KeyFormat format = RsaTool.KeyFormat.of(keyFormat);
        if (format == null) {
            format = RsaTool.KeyFormat.PKCS8;
        }
        RsaTool rsaTool = new RsaTool(format, RsaTool.KeyLength.LENGTH_2048);
        return rsaTool.createKeys();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RsaTool.KeyStore resetAppKeys(Long appId, Integer keyFormat) throws Exception {
        RsaTool.KeyStore keyStore = createKeys(keyFormat);

        AppKeysDto dto = new AppKeysDto();
        dto.setKeyFormat(keyFormat);
        dto.setAppId(appId);
        dto.setPrivateKeyApp(keyStore.getPrivateKey());
        dto.setPublicKeyApp(keyStore.getPublicKey());
        appKeysService.updateAppKeys(dto);
        return keyStore;
    }

    @Override
    public AppKeysVo getKeys(Long appId, Boolean showPrivateKey) {
        App app = this.getById(appId);
        ArgumentAssert.notNull(app, "应用不存在");
        AppKeys appKeys = appKeysService.getByAppId(appId);

        AppKeysVo vo;
        if (appKeys != null) {
            vo = BeanUtil.copyProperties(appKeys, AppKeysVo.class);
        } else {
            vo = new AppKeysVo();
            vo.setKeyFormat(RsaTool.KeyFormat.PKCS8.getCode());
        }

        // 私钥不能提供给开发者
        if (showPrivateKey == null || !showPrivateKey) {
            vo.setPrivateKeyApp(null);
            vo.setPrivateKeyPlatform(null);
        }
        vo.setAppId(appId);
        vo.setAppKey(app.getAppKey());
        vo.setAppSecret(app.getAppSecret());
        vo.setAppName(app.getName());
        return vo;
    }
}
