package top.mddata.console.message.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.message.dto.InterfaceConfigSettingDto;
import top.mddata.console.message.entity.InterfaceConfig;

/**
 * 接口 服务层。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:47
 */
public interface InterfaceConfigService extends SuperService<InterfaceConfig> {

    /**
     * 根据接口标识查询接口配置。
     * @param key 接口标识
     * @return 接口配置
     */
    InterfaceConfig getByKey(String key);

    /**
     * 更新接口配置。
     *
     * @param dto 接口配置
     * @return 主键ID
     */
    Long updateConfigById(InterfaceConfigSettingDto dto);

    /**
     * 检查接口标识是否存在
     * @param key 接口标识
     * @param id 接口id
     * @return true=存在 false=不存在
     */
    Boolean check(String key, Long id);
}
