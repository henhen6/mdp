package top.mddata.base.apiperm.engine;

import cn.hutool.core.util.StrUtil;
import top.mddata.base.apiperm.model.ApiPattern;
import top.mddata.base.apiperm.model.UserApiPerm;
import top.mddata.base.apiperm.spi.ApiPermProvider;

import java.util.Collection;
import java.util.Set;

/**
 * 接口权限判定引擎（纯逻辑，无 web 依赖，单体/网关共用）。
 */
public final class ApiPermChecker {
    private ApiPermChecker() {
    }

    /**
     * 判定结果：true 放行；false 拒绝。
     * 判定链：开关 → 纳管判定 → 运营者豁免 → 用户放行集匹配。
     */
    public static boolean check(ApiPermProvider provider, String rawPath, String method, Long userId) {
        if (!provider.isAuthEnabled()) {
            return true;
        }
        String path = normalizePath(rawPath, provider.getGatewayPrefix(), provider.getServicePrefixes());
        if (!isManaged(provider.findAllPatterns(), path, method)) {
            return provider.isNotConfigAllow();
        }
        UserApiPerm userPerm = provider.findUserPerm(userId);
        if (userPerm.isOperationsAdmin()) {
            return true;
        }
        return userPerm.getPatterns().stream().anyMatch(p -> p.matches(path, method));
    }

    /** 任一已配置模式命中即纳管 */
    public static boolean isManaged(Collection<ApiPattern> allPatterns, String path, String method) {
        return allPatterns.stream().anyMatch(p -> p.matches(path, method));
    }

    /** 剥离「网关前缀 + 第一段服务前缀」，得到裸路径 */
    public static String normalizePath(String rawPath, String gatewayPrefix, Set<String> servicePrefixes) {
        String path = rawPath;
        if (StrUtil.isNotEmpty(gatewayPrefix) && path.startsWith("/" + gatewayPrefix + "/")) {
            path = path.substring(gatewayPrefix.length() + 1);
        }
        String first = StrUtil.subBetween(path, "/", "/");
        if (first != null && servicePrefixes.contains(first)) {
            path = path.substring(first.length() + 1);
        }
        return path;
    }
}
