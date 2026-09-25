package top.mddata.base.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import top.mddata.base.constant.ContextConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 上下文工具类
 * <p>
 * 用于管理和访问当前线程上下文中的用户信息、应用信息、组织信息等。
 * 基于 ThreadLocal 实现，支持在同一个请求链路中传递上下文数据。
 * </p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>用户信息管理：用户ID、token等</li>
 *   <li>应用信息管理：应用ID等</li>
 *   <li>组织信息管理：公司ID、部门ID等</li>
 *   <li>链路追踪：traceId、灰度版本等</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 设置用户ID
 * ContextUtil.setUserId(12345L);
 *
 * // 获取用户ID
 * Long userId = ContextUtil.getUserId();
 *
 * // 请求结束后清理上下文（重要！）
 * ContextUtil.remove();
 * </pre>
 *
 * <p><b>注意事项：</b></p>
 * <ul>
 *   <li>必须在使用完毕后调用 {@link #remove()} 方法清理 ThreadLocal，避免内存泄漏</li>
 *   <li>建议在拦截器或过滤器中统一进行清理</li>
 *   <li>跨线程传递时需要手动复制上下文数据</li>
 * </ul>
 *
 * @author henhen6
 * @since 2017-12-13 16:52
 */
public final class ContextUtil {

    /**
     * 线程本地变量存储
     * <p>
     * 用于在同一个请求链路中传递上下文数据。
     * 使用 ConcurrentHashMap 作为底层存储，支持并发访问。
     * </p>
     *
     * <p><b>重要：</b>必须在使用完毕后调用 {@link #remove()} 清理，避免内存泄漏。</p>
     */
    private static final ThreadLocal<Map<String, String>> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 私有构造函数，防止实例化
     */
    private ContextUtil() {
    }

    /**
     * 批量设置上下文参数
     * <p>
     * 将指定 Map 中的所有键值对设置到当前线程上下文中。
     * 通常用于在请求入口处批量初始化上下文数据。
     * </p>
     *
     * @param map 包含上下文参数的 Map
     */
    public static void putAll(Map<String, String> map) {
        map.forEach(ContextUtil::set);
    }

    /**
     * 设置上下文参数
     * <p>
     * 将指定的键值对设置到当前线程上下文中。
     * 如果值为空或空白字符串，则忽略该设置。
     * </p>
     *
     * @param key   参数键
     * @param value 参数值，会被转换为字符串
     */
    public static void set(String key, Object value) {
        if (ObjectUtil.isEmpty(value) || StrUtil.isBlankOrUndefined(value.toString())) {
            return;
        }
        Map<String, String> map = getLocalMap();
        map.put(key, value.toString());
    }

    /**
     * 获取上下文参数（指定类型）
     * <p>
     * 从当前线程上下文中获取指定键的值，并转换为目标类型。
     * 如果键不存在，返回 null。
     * </p>
     *
     * @param key   参数键
     * @param type  目标类型
     * @param <T>   返回值类型
     * @return 转换后的值，键不存在时返回 null
     */
    public static <T> T get(String key, Class<T> type) {
        Map<String, String> map = getLocalMap();
        return Convert.convert(type, map.get(key));
    }

    /**
     * 获取上下文参数（指定类型和默认值）
     * <p>
     * 从当前线程上下文中获取指定键的值，并转换为目标类型。
     * 如果键不存在或值为空，则返回指定的默认值。
     * </p>
     *
     * @param key   参数键
     * @param type  目标类型
     * @param def   默认值
     * @param <T>   返回值类型
     * @return 转换后的值，键不存在时返回默认值
     */
    public static <T> T get(String key, Class<T> type, Object def) {
        Map<String, String> map = getLocalMap();
        String value;
        if (def == null) {
            value = map.get(key);
        } else {
            value = map.getOrDefault(key, String.valueOf(def));
        }
        return Convert.convert(type, StrUtil.isEmpty(value) ? def : value);
    }

    /**
     * 获取上下文参数（字符串类型）
     * <p>
     * 从当前线程上下文中获取指定键的值。
     * 如果键不存在，返回空字符串。
     * </p>
     *
     * @param key 参数键
     * @return 参数值，键不存在时返回空字符串
     */
    public static String get(String key) {
        Map<String, String> map = getLocalMap();
        return map.getOrDefault(key, StrPool.EMPTY);
    }

    /**
     * 获取当前线程的上下文 Map
     * <p>
     * 返回当前线程的上下文数据 Map。
     * 如果当前线程还没有上下文，会自动创建一个新的 ConcurrentHashMap。
     * </p>
     *
     * @return 上下文数据 Map
     */
    public static Map<String, String> getLocalMap() {
        Map<String, String> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new ConcurrentHashMap<>(10);
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    /**
     * 设置当前线程的上下文 Map
     * <p>
     * 替换当前线程的整个上下文数据。
     * 通常用于从其他线程复制上下文数据。
     * </p>
     *
     * @param localMap 新的上下文数据 Map
     */
    public static void setLocalMap(Map<String, String> localMap) {
        THREAD_LOCAL.set(localMap);
    }

    /**
     * 获取当前语言环境
     * <p>
     * 从上下文中获取前端传递的多语言环境标识，用于国际化支持。
     * </p>
     *
     * @return 语言环境标识，如 "zh-CN"、"en-US" 等
     */
    public static String getLocale() {
        return get(ContextConstants.LOCALE, String.class, StrPool.EMPTY);
    }
    
    /**
     * 设置当前语言环境
     *
     * @param locale 语言环境标识
     */
    public static void setLocale(Object locale) {
        set(ContextConstants.LOCALE, locale == null ? StrPool.EMPTY : locale);
    }
    /**
     * 获取当前用户ID
     * <p>
     * 从上下文中获取当前登录用户的唯一标识。
     * 该值通常在用户登录时从 JWT Token 中解析并设置。
     * </p>
     *
     * @return 用户ID，未登录时返回 null
     */
    public static Long getUserId() {
        return get(ContextConstants.USER_ID, Long.class);
    }

    /**
     * 设置当前用户ID
     * <p>
     * 将用户ID设置到当前线程上下文中。
     * 通常在用户登录成功后的拦截器中调用。
     * </p>
     *
     * @param userId 用户ID
     */
    public static void setUserId(Object userId) {
        set(ContextConstants.USER_ID, userId);
    }

    /**
     * 判断用户ID是否为空
     * <p>
     * 检查当前上下文中是否存在有效的用户ID。
     * 用户ID为 null、"null" 或 "0" 时视为空。
     * </p>
     *
     * @return true 表示用户ID为空，false 表示有效
     */
    public static boolean isEmptyUserId() {
        return isEmptyLong(ContextConstants.USER_ID);
    }

    /**
     * 判断应用ID是否为空
     * <p>
     * 检查当前上下文中是否存在有效的应用ID。
     * </p>
     *
     * @return true 表示应用ID为空，false 表示有效
     */
    public static boolean isEmptyAppId() {
        return isEmptyLong(ContextConstants.APP_ID);
    }


    /**
     * 获取当前应用ID
     * <p>
     * 从上下文中获取当前请求所属的应用标识。
     * 用于多应用场景下的应用识别和权限控制。
     * </p>
     *
     * @return 应用ID，未指定时返回 null
     */
    public static Long getAppId() {
        return get(ContextConstants.APP_ID, Long.class);
    }

    /**
     * 设置当前应用ID
     *
     * @param appId 应用ID
     */
    public static void setAppId(Object appId) {
        set(ContextConstants.APP_ID, appId);
    }

    /**
     * 获取当前请求路径
     * <p>
     * 从上下文中获取前端地址栏 # 号后的路径信息。
     * 用于日志记录、权限校验等场景。
     * </p>
     *
     * @return 请求路径，未设置时返回空字符串
     */
    public static String getPath() {
        return get(ContextConstants.PATH, String.class, StrPool.EMPTY);
    }

    /**
     * 设置当前请求路径
     *
     * @param path 请求路径
     */
    public static void setPath(Object path) {
        set(ContextConstants.PATH, path == null ? StrPool.EMPTY : path);
    }

    /**
     * 获取当前用户 Token
     * <p>
     * 从上下文中获取当前用户的认证令牌。
     * 用于服务间调用时传递用户身份。
     * </p>
     *
     * @return 用户 Token
     */
    public static String getToken() {
        return get(ContextConstants.TOKEN, String.class);
    }

    /**
     * 设置当前用户 Token
     *
     * @param token 用户 Token
     */
    public static void setToken(String token) {
        set(ContextConstants.TOKEN, token == null ? StrPool.EMPTY : token);
    }

    /**
     * 获取当前公司ID
     * <p>
     * 从上下文中获取用户当前所属公司的唯一标识。
     * 用于多公司场景下的数据隔离和权限控制。
     * </p>
     *
     * @return 公司ID
     */
    public static Long getCurrentCompanyId() {
        return get(ContextConstants.COMPANY_ID, Long.class);
    }

    /**
     * 设置当前公司ID
     *
     * @param val 公司ID
     */
    public static void setCurrentCompanyId(Object val) {
        set(ContextConstants.COMPANY_ID, val);
    }


    /**
     * 获取当前公司组织性质
     * <p>
     * 从上下文中获取用户当前所属公司的组织性质标识。
     * </p>
     *
     * @return 公司组织性质
     */
    public static Integer getCurrentCompanyNature() {
        return get(ContextConstants.COMPANY_NATURE, Integer.class);
    }

    /**
     * 设置当前公司组织性质
     *
     * @param val 公司组织性质
     */
    public static void setCurrentCompanyNature(Object val) {
        set(ContextConstants.COMPANY_NATURE, val);
    }


    /**
     * 获取当前部门ID
     * <p>
     * 从上下文中获取用户当前所属部门的唯一标识。
     * 用于部门级别的数据隔离和权限控制。
     * </p>
     *
     * @return 部门ID
     */
    public static Long getCurrentDeptId() {
        return get(ContextConstants.DEPT_ID, Long.class);
    }

    /**
     * 设置当前部门ID
     *
     * @param val 部门ID
     */
    public static void setCurrentDeptId(Object val) {
        set(ContextConstants.DEPT_ID, val);
    }

    /**
     * 获取当前顶级公司组织性质
     * <p>
     * 从上下文中获取用户当前所属顶级公司的组织性质标识。
     * </p>
     *
     * @return 顶级公司组织性质
     */
    public static Integer getCurrentTopCompanyNature() {
        return get(ContextConstants.TOP_COMPANY_NATURE, Integer.class);
    }

    /**
     * 设置当前顶级公司组织性质
     *
     * @param val 顶级公司组织性质
     */
    public static void setCurrentTopCompanyNature(Object val) {
        set(ContextConstants.TOP_COMPANY_NATURE, val);
    }

    /**
     * 获取当前顶级公司ID
     * <p>
     * 从上下文中获取用户当前所属顶级公司的唯一标识。
     * 用于集团级别的数据隔离和权限控制。
     * </p>
     *
     * @return 顶级公司ID
     */
    public static Long getCurrentTopCompanyId() {
        return get(ContextConstants.TOP_COMPANY_ID, Long.class);
    }

    /**
     * 设置当前顶级公司ID
     *
     * @param val 顶级公司ID
     */
    public static void setCurrentTopCompanyId(Object val) {
        set(ContextConstants.TOP_COMPANY_ID, val);
    }


    private static boolean isEmptyLong(String key) {
        String val = getLocalMap().get(key);
        return StrUtil.isEmpty(val) || StrPool.NULL.equals(val) || StrPool.ZERO.equals(val);
    }

    private static boolean isEmptyStr(String key) {
        String val = getLocalMap().get(key);
        return val == null || StrPool.NULL.equals(val);
    }

    /**
     * 获取链路追踪 ID
     * <p>
     * 从上下文中获取日志链路追踪的唯一标识。
     * 用于在分布式系统中追踪一个请求的完整调用链路。
     * </p>
     *
     * @return 链路追踪 ID
     */
    public static String getLogTraceId() {
        return get(ContextConstants.TRACE, String.class);
    }

    /**
     * 设置链路追踪 ID
     *
     * @param val 链路追踪 ID
     */
    public static void setLogTraceId(String val) {
        set(ContextConstants.TRACE, val);
    }


    /**
     * 获取灰度发布版本号
     * <p>
     * 从上下文中获取当前请求的灰度版本号。
     * 用于灰度发布场景下的流量控制和版本路由。
     * </p>
     *
     * @return 灰度版本号
     */
    public static String getGrayVersion() {
        return get(ContextConstants.GRAY_VERSION, String.class);
    }

    /**
     * 设置灰度发布版本号
     *
     * @param val 灰度版本号
     */
    public static void setGrayVersion(String val) {
        set(ContextConstants.GRAY_VERSION, val);
    }

    /**
     * 判断是否允许继续执行
     * <p>
     * 用于演示环境下的操作控制。
     * 当上下文中存在 proceed 标识时，表示允许执行某些敏感操作（如 SQL 执行）。
     * </p>
     *
     * @return true 表示允许执行，false 表示禁止执行
     */
    public static Boolean isProceed() {
        String proceed = get(ContextConstants.PROCEED, String.class);
        return StrUtil.isNotEmpty(proceed);
    }


    /**
     * 清理当前线程的上下文数据
     * <p>
     * <b>重要：</b>必须在请求结束时调用此方法，清理 ThreadLocal 中存储的数据，
     * 否则会导致内存泄漏，特别是在使用线程池的场景下。
     * </p>
     *
     * <p>建议在以下位置调用：</p>
     * <ul>
     *   <li>拦截器的 postHandle 或 afterCompletion 方法中</li>
     *   <li>过滤器的 finally 块中</li>
     * </ul>
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
