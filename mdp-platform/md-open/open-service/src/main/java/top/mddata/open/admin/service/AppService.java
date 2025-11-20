package top.mddata.open.admin.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.admin.entity.App;
import top.mddata.open.admin.utils.RsaTool;
import top.mddata.open.admin.vo.AppKeysVo;
import top.mddata.open.admin.vo.AppVo;
import top.mddata.open.client.dto.AppDevInfoDto;
import top.mddata.open.client.dto.AppInfoUpdateDto;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
public interface AppService extends SuperService<App> {

    /**
     * 查询需要 接收事件推送的应用
     *
     * @return 应用
     */
    List<AppVo> listNeedPushApp();

    /**
     * 根据应用标识查询应用
     *
     * @param appKey 应用标识
     * @return 应用
     */
    AppVo getAppByAppKey(String appKey);

    /**
     * 查询用户能访问的应用
     *
     * @param userId 用户id
     * @return 应用列表
     */
    List<AppVo> listMyApp(Long userId);


    /**
     * 生成秘钥
     *
     * @param keyFormat 秘钥格式，1：PKCS8(JAVA适用)，2：PKCS1(非JAVA适用)
     * @return 秘钥
     */
    RsaTool.KeyStore createKeys(Integer keyFormat) throws Exception;

    /**
     * 获取秘钥信息
     *
     * @param showPrivateKey 是否显示私钥
     * @param appId  应用ID
     * @return 秘钥
     */
    AppKeysVo getKeys(Long appId, Boolean showPrivateKey);

    /**
     * 修改应用信息
     *
     * @param dto 应用信息
     * @return 应用主键
     */
    Long updateInfoById(AppInfoUpdateDto dto);

    /**
     * 修改应用开发信息
     *
     * @param dto 应用开发信息
     * @return 应用主键
     */
    Long updateDevById(AppDevInfoDto dto);

    /**
     * 重置开发者秘钥
     *
     * @param appId 应用ID
     * @param keyFormat     秘钥格式，1：PKCS8(JAVA适用)，2：PKCS1(非JAVA适用)
     * @return 秘钥
     * @throws Exception 异常
     */
    RsaTool.KeyStore resetAppKeys(Long appId, Integer keyFormat) throws Exception;
}
