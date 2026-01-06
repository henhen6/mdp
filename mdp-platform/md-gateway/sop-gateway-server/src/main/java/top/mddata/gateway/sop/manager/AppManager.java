package top.mddata.gateway.sop.manager;

import top.mddata.gateway.sop.pojo.dto.ApiDto;
import top.mddata.gateway.sop.pojo.dto.AppDto;

/**
 *
 * @author tangyh
 * @since 2026/1/6 16:44
 */
public interface AppManager {
    /**
     * 根据appKey获取app信息
     *
     * @param appKey appKey
     * @return AppDto
     */
    AppDto getByAppKey(String appKey);

    /**
     * 判断当前应用是否有权限访问该开放接口
     *
     * @param id      应用ID
     * @param apiDto  开放接口
     * @return boolean
     */
    boolean hasPermission(Long id, ApiDto apiDto);
}
