package top.mddata.open.admin.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.admin.dto.AppKeysDto;
import top.mddata.open.admin.entity.AppKeys;
import top.mddata.open.admin.utils.RsaTool;
import top.mddata.open.admin.vo.AppKeysVo;
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
     * 获取秘钥信息
     *
     * @param showPrivateKey 是否显示私钥
     * @param appId  应用ID
     * @return 秘钥
     */
    AppKeysVo getKeys(Long appId, Boolean showPrivateKey);


    /**
     * 生成秘钥
     *
     * @param keyFormat 秘钥格式，1：PKCS8(JAVA适用)，2：PKCS1(非JAVA适用)
     * @return 秘钥
     */
    RsaTool.KeyStore createKeys(Integer keyFormat) throws Exception;


    /**
     * 重置开发者秘钥
     *
     * @param appId 应用ID
     * @param keyFormat     秘钥格式，1：PKCS8(JAVA适用)，2：PKCS1(非JAVA适用)
     * @return 秘钥
     * @throws Exception 异常
     */
    RsaTool.KeyStore resetAppKeys(Long appId, Integer keyFormat) throws Exception;

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
