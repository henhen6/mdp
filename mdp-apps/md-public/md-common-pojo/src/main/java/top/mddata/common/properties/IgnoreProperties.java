package top.mddata.common.properties;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.AntPathMatcher;
import top.mddata.base.constant.Constants;
import top.mddata.common.enumeration.HttpMethod;

import java.util.Map;
import java.util.Set;

import static top.mddata.base.utils.CollHelper.putAll;


/**
 * 忽略token 配置类
 * <p>
 * 1. 是否需要租户信息?
 * 2. 是否需要用户信息?
 * 3. 是否需要uri权限?
 *
 * @author henhen6
 * @since 2025/01/03
 */
@Data
@ConfigurationProperties(prefix = IgnoreProperties.PREFIX)
public class IgnoreProperties {
    public static final String PREFIX = Constants.PROJECT_PREFIX + ".ignore";
    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
    /**
     * 是否启用网关的 uri权限鉴权 和 前端按钮权限 (设置为false，则不校验访问权限)
     *
     * @see 4.0.0
     */
    private Boolean authEnabled = true;
    /**
     * 未配置（未纳管）接口是否放行。
     * true：只拦截已配置接口（联调期推荐）；false：未配置一律拒绝（白名单）。
     */
    private Boolean notConfigUriAllow = true;
    /**
     * 是否启用字段权限鉴权（响应层字段隐藏/脱敏）。
     * false：所有用户看到字段原文（新功能灰度期可关闭）。
     */
    private Boolean fieldAuthEnabled = true;
    /**
     * 网关前缀，接口权限路径归一化时剥离
     */
    private String gatewayPrefix = "api";
    /**
     * 服务前缀集合，接口权限路径归一化时剥离第一段
     */
    private Set<String> servicePrefixes = CollUtil.newHashSet("console", "workbench", "open");
    /**
     * 前端校验按钮 是否区分大小写
     */
    private Boolean caseSensitive = false;

    private Map<String, Set<String>> baseUri = MapUtil.<String, Set<String>>builder(HttpMethod.ALL.name(), CollUtil.newHashSet(
            "/**/{p:[a-zA-Z0-9]+}.css",
            "/**/{p:[a-zA-Z0-9]+}.js",
            "/**/{p:[a-zA-Z0-9]+}.html",
            "/**/{p:[a-zA-Z0-9]+}.ico",
            "/**/{p:[a-zA-Z0-9]+}.jpg",
            "/**/{p:[a-zA-Z0-9]+}.jpeg",
            "/**/{p:[a-zA-Z0-9]+}.png",
            "/**/{p:[a-zA-Z0-9]+}.gif",
            "/**/v2/**",
            "/**/api-docs/**",
            "/**/api-docs-ext/**",
            "/**/swagger-resources/**",
            "/**/webjars/**",
            "/actuator/**",
            "/**/static/**",
            "/**/public/**",
            "/error",
            // 表单验证
            "/**/form/validator/**",
            // 不需要租户编码、不需要登录、不需要权限即可访问的接口
            "/**/anno/**",
            "/**/druid/**")).build();

    /**
     * 需要登录，但不需要校验权限。 即： 需要携带 token， 但不对uri权限验证
     * <p>
     * 注意： 此类接口，可以获取当前请求的用户信息(userId)
     * <p>
     * 如： 文件上传、字典查询等任何人都能访问的URI接口
     *
     * @see 1.0.0
     */
    private Map<String, Set<String>> anyone = MapUtil.newHashMap();


    /**
     * 不需要登录, 且不需要校验权限。 即： 不携带 token，也不校验接口权限
     * <p>
     * 注意： 此类接口，无法获取当前请求的用户信息(userId)
     * 注意： 此类接口，无法获取控制URI权限
     * <p>
     * 如： 门户网站的接口，登录页面获取系统配置的接口等
     *
     * @see 1.0.0
     */
    private Map<String, Set<String>> anyUser = MapUtil.newHashMap();


    public Map<String, Set<String>> buildAnyone() {
        return putAll(getBaseUri(), this.getAnyUser(), this.getAnyone());
    }

    public Map<String, Set<String>> buildAnyUser() {
        return putAll(getBaseUri(), this.getAnyUser());
    }

    /**
     * 是否忽略uri权限认证
     *
     * @param path 相对路径
     * @return
     */
    public boolean isIgnoreUriAuth(String method, String path) {
        Map<String, Set<String>> all = putAll(getBaseUri(), this.getAnyUser(), this.getAnyone());

        return isIgnore(method, path, all);
    }


    /**
     * 是否忽略登录
     *
     * @param path 相对路径
     * @return
     */
    public boolean isIgnoreUser(String method, String path) {
        Map<String, Set<String>> all = putAll(getBaseUri(), this.getAnyUser());
        return isIgnore(method, path, all);
    }


    private boolean isIgnore(String method, String path, Map<String, Set<String>> all) {
        for (Map.Entry<String, Set<String>> entry : all.entrySet()) {
            String m = entry.getKey();
            Set<String> paths = entry.getValue();
            boolean match;
            if (HttpMethod.ALL.name().equalsIgnoreCase(m)) {
                match = paths.stream().anyMatch(url -> ANT_PATH_MATCHER.match(url, path));
            } else {
                match = m.equalsIgnoreCase(method)
                        && paths.stream().anyMatch(url -> ANT_PATH_MATCHER.match(url, path));
            }
            if (match) {
                return true;
            }
        }
        return false;
    }
}
