package top.mddata.gateway.sop.manager;

import top.mddata.gateway.sop.common.ApiDto;

/**
 *
 * @author tangyh
 * @since 2026/1/6 16:44
 */
public interface ApiManager {
    /**
     * 获取api信息
     *
     * @param method  方法名
     * @param version 版本
     * @return ApiDto
     */
    ApiDto getByMethodAndVersion(String method, String version);
}
