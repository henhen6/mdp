package top.mddata.base.apiperm.model;

import org.springframework.util.AntPathMatcher;

/**
 * 已配置接口模式：uri 模式 + 请求方式（ALL 匹配任意方式）。
 *
 * @param uri    接口 URI 模式（支持 Ant 风格通配）
 * @param method 请求方式，ALL 表示匹配任意方式
 */
public record ApiPattern(String uri, String method) {
    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static final String ALL = "ALL";

    public boolean matches(String path, String requestMethod) {
        if (!ALL.equalsIgnoreCase(method) && !method.equalsIgnoreCase(requestMethod)) {
            return false;
        }
        return MATCHER.match(uri, path);
    }
}
