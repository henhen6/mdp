package top.mddata.open.service.app.impl;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.sop.support.util.RsaTool;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.open.AppKeysCkBuilder;
import top.mddata.open.dto.admin.AppKeysDto;
import top.mddata.open.dto.client.AppEventSubscriptionDto;
import top.mddata.open.dto.client.AppKeysUpdateDto;
import top.mddata.open.entity.admin.App;
import top.mddata.open.entity.admin.AppKeys;
import top.mddata.open.entity.admin.EventSubscription;
import top.mddata.open.mapper.admin.AppKeysMapper;
import top.mddata.open.service.app.AppKeysService;
import top.mddata.open.service.app.AppService;
import top.mddata.open.service.notify.event.EventSubscriptionService;
import top.mddata.open.vo.admin.AppKeysVo;
import top.mddata.open.vo.admin.AppVo;

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
        CacheKey cacheKey = AppKeysCkBuilder.builder(appId);
        CacheResult<AppKeys> result = cacheOps.get(cacheKey, k -> super.getOne(QueryWrapper.create().eq(AppKeys::getAppId, appId)));
        return result.getValue();
    }

    @Override
    @Transactional(readOnly = true)
    public AppKeysVo getByAppKey(String appKey) {
        ArgumentAssert.notEmpty(appKey, "appKey不能为空");
        AppVo appVo = appService.getAppByAppKey(appKey);
        if (appVo == null) {
            return null;
        }
        AppKeys appKeys = getByAppId(appVo.getId());
        if (appKeys == null) {
            return null;
        }
        AppKeysVo vo = BeanUtil.copyProperties(appKeys, AppKeysVo.class);
        vo.setAppKey(appVo.getAppKey());
        vo.setAppSecret(appVo.getAppSecret());
        vo.setAppName(appVo.getName());
        return vo;
    }

    @Override
    public AppKeysVo getKeys(Long appId) {
        App app = appService.getByIdCache(appId);
        ArgumentAssert.notNull(app, "应用不存在");
        AppKeys appKeys = getByAppId(appId);

        AppKeysVo vo;
        if (appKeys != null) {
            vo = BeanUtil.copyProperties(appKeys, AppKeysVo.class);
        } else {
            vo = new AppKeysVo();
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
    @Transactional(rollbackFor = Exception.class)
    public AppKeys saveDto(Object save) {
        AppKeysDto dto = (AppKeysDto) save;
        AppKeys appKeys = this.getOne(QueryWrapper.create().eq(AppKeys::getAppId, dto.getAppId()));
        if (appKeys == null) {
            appKeys = new AppKeys();
            appKeys.setAppId(dto.getAppId());
        }
        BeanUtils.copyProperties(dto, appKeys);

        if (Boolean.TRUE.equals(dto.getNotifyState())) {
            ArgumentAssert.notEmpty(dto.getNotifyUrl(), "请填写通知地址");
            ArgumentAssert.notNull(dto.getNotifyEncryptionType(), "请填写加密模式");

            eventSubscriptionService.saveEventSubscriptionByAppId(dto.getAppId(), dto.getEventTypeIdList());
        }

        saveOrUpdate(appKeys);
        delCacheByAppId(appKeys.getAppId());
        return appKeys;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppKeys updateKeysByClient(AppKeysUpdateDto dto) {
        AppKeys appKeys = this.getOne(QueryWrapper.create().eq(AppKeys::getAppId, dto.getAppId()));
        if (appKeys == null) {
            appKeys = new AppKeys();
            appKeys.setAppId(dto.getAppId());
        }
        BeanUtils.copyProperties(dto, appKeys);
        saveOrUpdate(appKeys);
        delCacheByAppId(appKeys.getAppId());
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
        BeanUtils.copyProperties(dto, appKeys);
        saveOrUpdate(appKeys);
        delCacheByAppId(appKeys.getAppId());
        return appKeys;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateEventSubscription(AppEventSubscriptionDto param) {
        AppKeys appKeys = this.getOne(QueryWrapper.create().eq(AppKeys::getAppId, param.getAppId()));
        if (appKeys == null) {
            appKeys = new AppKeys();
            appKeys.setAppId(param.getAppId());
        }
        appKeys.setNotifyUrl(param.getNotifyUrl());
        appKeys.setNotifyEncryptionType(param.getNotifyEncryptionType());
        appKeys.setNotifyState(param.getNotifyState());
        appKeys.setNotifyToken(param.getNotifyToken());
        appKeys.setNotifyEncodingAesKey(param.getNotifyEncodingAesKey());
        eventSubscriptionService.saveEventSubscriptionByAppId(param.getAppId(), param.getEventTypeIdList());

        saveOrUpdate(appKeys);
        delCacheByAppId(appKeys.getAppId());
        return appKeys.getAppId();
    }

    private void delCacheByAppId(Long appId) {
        CacheKey cacheKey = AppKeysCkBuilder.builder(appId);
        cacheOps.del(cacheKey);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public RsaTool.KeyStore resetAppKeys(Long appId, Integer keyFormat) throws Exception {
        RsaTool.KeyStore keyStore = createKeys(keyFormat);

        AppKeysDto dto = new AppKeysDto();
        dto.setAppId(appId);
        dto.setPublicKeyApp(keyStore.getPublicKey());
        updateAppKeys(dto);
        delCacheByAppId(appId);
        return keyStore;
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

}
