package top.mddata.open.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.open.admin.dto.AppKeysDto;
import top.mddata.open.admin.entity.App;
import top.mddata.open.admin.entity.AppKeys;
import top.mddata.open.admin.entity.EventSubscription;
import top.mddata.open.admin.mapper.AppKeysMapper;
import top.mddata.open.admin.service.AppKeysService;
import top.mddata.open.admin.service.AppService;
import top.mddata.open.admin.service.EventSubscriptionService;
import top.mddata.open.admin.utils.RsaTool;
import top.mddata.open.admin.vo.AppKeysVo;
import top.mddata.open.client.dto.AppEventSubscriptionDto;
import top.mddata.open.client.dto.AppKeysUpdateDto;

import java.util.List;

/**
 * 应用秘钥 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AppKeysServiceImpl extends SuperServiceImpl<AppKeysMapper, AppKeys> implements AppKeysService {
    private final EventSubscriptionService eventSubscriptionService;
    private final AppService appService;

    @Override
    @Transactional(readOnly = true)
    public AppKeys getByAppId(Long appId) {
        return super.getOne(QueryWrapper.create().eq(AppKeys::getAppId, appId));
    }


    @Override
    public AppKeysVo getKeys(Long appId, Boolean showPrivateKey) {
        App app = appService.getByIdCache(appId);
        ArgumentAssert.notNull(app, "应用不存在");
        AppKeys appKeys = getByAppId(appId);

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

        List<EventSubscription> eventSubscriptionList = eventSubscriptionService.list(QueryWrapper.create().eq(EventSubscription::getAppId, appId));
        List<Long> eventTypeIdList = eventSubscriptionList.stream().map(EventSubscription::getEventTypeId).toList();
        vo.setEventTypeIdList(eventTypeIdList);
        return vo;
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
        updateAppKeys(dto);
        return keyStore;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppKeys saveDto(Object save) {
        AppKeysDto dto = (AppKeysDto) save;
        AppKeys appKeys = this.getOne(QueryWrapper.create().eq(AppKeys::getAppId, dto.getAppId()));
        if (appKeys == null) {
            appKeys = new AppKeys();
            appKeys.setAppId(dto.getAppId());
        }
        BeanUtils.copyProperties(dto, appKeys);

        if (dto.getNotifyState()) {
            ArgumentAssert.notEmpty(dto.getNotifyUrl(), "请填写通知地址");
            ArgumentAssert.notNull(dto.getNotifyEncryptionType(), "请填写加密类型");

            eventSubscriptionService.saveEventSubscriptionByAppId(dto.getAppId(), dto.getEventTypeIdList());
        }

        saveOrUpdate(appKeys);
        // TODO henhen6 清理缓存
        return appKeys;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppKeys updateKeysByClient(AppKeysUpdateDto dto) {
        AppKeys appKeys = this.getOne(QueryWrapper.create().eq(AppKeys::getAppId, dto.getAppId()));
        if (appKeys == null) {
            appKeys = new AppKeys();
            appKeys.setAppId(dto.getAppId());
            appKeys.setPrivateKeyPlatform("");
            appKeys.setPublicKeyPlatform("");
        }
        BeanUtils.copyProperties(dto, appKeys);
        saveOrUpdate(appKeys);
        // TODO henhen6 清理缓存
        return appKeys;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppKeys updateAppKeys(AppKeysDto dto) {
        AppKeys appKeys = this.getOne(QueryWrapper.create().eq(AppKeys::getAppId, dto.getAppId()));
        if (appKeys == null) {
            appKeys = new AppKeys();
            appKeys.setAppId(dto.getAppId());
        }
        appKeys.setKeyFormat(dto.getKeyFormat());
        appKeys.setPrivateKeyApp(dto.getPrivateKeyApp());
        appKeys.setPublicKeyApp(dto.getPublicKeyApp());

        saveOrUpdate(appKeys);
        // TODO henhen6 清理缓存
        return appKeys;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateEventSubscription(AppEventSubscriptionDto param) {
        AppKeys appKeys = this.getOne(QueryWrapper.create().eq(AppKeys::getAppId, param.getAppId()));
        if (appKeys == null) {
            appKeys = new AppKeys();
            appKeys.setAppId(param.getAppId());
            appKeys.setPrivateKeyPlatform("");
            appKeys.setPublicKeyPlatform("");
            appKeys.setPublicKeyApp("");
        }
        appKeys.setNotifyUrl(param.getNotifyUrl());
        appKeys.setNotifyEncryptionType(param.getNotifyEncryptionType());
        appKeys.setNotifyState(param.getNotifyState());
        eventSubscriptionService.saveEventSubscriptionByAppId(param.getAppId(), param.getEventTypeIdList());

        saveOrUpdate(appKeys);
        return appKeys.getAppId();
    }
}
