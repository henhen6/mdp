package top.mddata.open.admin.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.admin.dto.AppKeysDto;
import top.mddata.open.admin.entity.AppKeys;
import top.mddata.open.client.dto.AppEventSubscriptionDto;
import top.mddata.open.client.dto.AppKeysUpdateDto;

/**
 * 应用秘钥 服务层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
public interface AppKeysService extends SuperService<AppKeys> {
    /**
     * 通过应用ID查询 秘钥
     *
     * @param appId 应用ID
     * @return 秘钥
     */
    AppKeys getByAppId(Long appId);

    /**
     * 修改应用秘钥信息
     *
     * @param param 公钥参数
     * @return 应用秘钥实体
     */
    AppKeys updateKeysByClient(AppKeysUpdateDto param);

    /**
     * 修改 应用公私钥
     *
     * @param param 公钥参数
     * @return 应用秘钥实体
     */
    AppKeys updateAppKeys(AppKeysDto param);

    /**
     * 修改应用事件订阅
     *
     * @param param 应用事件参数
     * @return 应用id
     */
    Long updateEventSubscription(AppEventSubscriptionDto param);
}
