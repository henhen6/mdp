package top.mddata.open.admin.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.open.admin.dto.AppKeysDto;
import top.mddata.open.admin.entity.AppKeys;
import top.mddata.open.admin.mapper.AppKeysMapper;
import top.mddata.open.admin.service.AppKeysService;
import top.mddata.open.admin.service.EventSubscriptionService;
import top.mddata.open.client.dto.AppEventSubscriptionDto;
import top.mddata.open.client.dto.AppKeysUpdateDto;

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

    @Override
    @Transactional(readOnly = true)
    public AppKeys getByAppId(Long appId) {
        return super.getOne(QueryWrapper.create().eq(AppKeys::getAppId, appId));
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
        AppKeys appKeys = this.getOne(QueryWrapper.create().eq(AppKeys::getAppId, param.getId()));
        if (appKeys == null) {
            appKeys = new AppKeys();
            appKeys.setAppId(param.getId());
        }
        appKeys.setNotifyUrl(param.getNotifyUrl());
        appKeys.setNotifyEncryptionType(param.getNotifyEncryptionType());
        appKeys.setNotifyState(param.getNotifyState());
        eventSubscriptionService.saveEventSubscriptionByAppId(param.getId(), param.getEventTypeIdList());

        saveOrUpdate(appKeys);
        return appKeys.getAppId();
    }
}
