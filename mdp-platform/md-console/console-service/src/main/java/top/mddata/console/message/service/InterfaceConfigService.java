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
     * 更新接口配置。
     *
     * @param dto 接口配置
     * @return 主键ID
     */
    Long updateConfigById(InterfaceConfigSettingDto dto);
}
