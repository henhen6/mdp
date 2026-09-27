package top.mddata.base.apiperm.spi;

import top.mddata.base.apiperm.model.ApiPattern;
import top.mddata.base.apiperm.model.UserApiPerm;

import java.util.List;
import java.util.Set;

/**
 * 接口权限数据提供方 SPI（实现方负责缓存）。
 */
public interface ApiPermProvider {
    /** 是否启用接口权限鉴权 */
    boolean isAuthEnabled();

    /** 未配置（未纳管）接口是否放行 */
    boolean isNotConfigAllow();

    /** 网关前缀（如 api），归一化时剥离 */
    String getGatewayPrefix();

    /** 服务前缀集合（如 console/workbench/open），归一化时剥离 */
    Set<String> getServicePrefixes();

    /** 全量已配置接口（缓存A） */
    List<ApiPattern> findAllPatterns();

    /** 当前用户放行集（缓存B） */
    UserApiPerm findUserPerm(Long userId);
}
