package top.mddata.base.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import top.mddata.base.constant.ContextConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 获取当前线程变量中的 用户id、用户昵称、租户编码、账号等信息
 *
 * @author henhen6
 * @since 2017-12-13 16:52
 */
public final class ContextUtil {

    /**
     * 支持多线程传递参数
     *
     * @create [2021/6/23 9:26 下午 ] [henhen6] [初始创建]
     * @since 2021/6/23 9:26 下午
     */
    private static final ThreadLocal<Map<String, String>> THREAD_LOCAL = new ThreadLocal<>();
//    private static final ThreadLocal<Map<String, String>> THREAD_LOCAL = new TransmittableThreadLocal<>();  // 配合 @Async 使用时会有问题

    private ContextUtil() {
    }

    public static void putAll(Map<String, String> map) {
        map.forEach(ContextUtil::set);
    }

    public static void set(String key, Object value) {
        if (ObjectUtil.isEmpty(value) || StrUtil.isBlankOrUndefined(value.toString())) {
            return;
        }
        Map<String, String> map = getLocalMap();
        map.put(key, value.toString());
    }

    public static <T> T get(String key, Class<T> type) {
        Map<String, String> map = getLocalMap();
        return Convert.convert(type, map.get(key));
    }

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

    public static String get(String key) {
        Map<String, String> map = getLocalMap();
        return map.getOrDefault(key, StrPool.EMPTY);
    }

    public static Map<String, String> getLocalMap() {
        Map<String, String> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new ConcurrentHashMap<>(10);
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    public static void setLocalMap(Map<String, String> localMap) {
        THREAD_LOCAL.set(localMap);
    }

    /**
     * 用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        return get(ContextConstants.USER_ID_HEADER, Long.class);
    }

    /**
     * 用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Object userId) {
        set(ContextConstants.USER_ID_HEADER, userId);
    }

    public static boolean isEmptyUserId() {
        return isEmptyLong(ContextConstants.USER_ID_HEADER);
    }

    public static boolean isEmptyApplicationId() {
        return isEmptyLong(ContextConstants.APPLICATION_ID_HEADER);
    }


    /**
     * 应用ID
     */
    public static Long getApplicationId() {
        return get(ContextConstants.APPLICATION_ID_HEADER, Long.class);
    }

    /**
     * 应用ID
     *
     * @param applicationId 应用ID
     */
    public static void setApplicationId(Object applicationId) {
        set(ContextConstants.APPLICATION_ID_HEADER, applicationId);
    }

    /**
     * 地址栏路径
     */
    public static String getPath() {
        return get(ContextConstants.PATH_HEADER, String.class, StrPool.EMPTY);
    }

    /**
     * 地址栏路径
     *
     * @param path 地址栏路径
     */
    public static void setPath(Object path) {
        set(ContextConstants.PATH_HEADER, path == null ? StrPool.EMPTY : path);
    }

    /**
     * 获取token
     *
     * @return token
     */
    public static String getToken() {
        return get(ContextConstants.TOKEN_HEADER, String.class);
    }

    public static void setToken(String token) {
        set(ContextConstants.TOKEN_HEADER, token == null ? StrPool.EMPTY : token);
    }

    /**
     * 获取 当前所属的公司ID
     *
     * @return java.lang.Long
     * @create [2022/9/9 4:50 PM ] [henhen6] [初始创建]
     * @since 2022/9/9 4:50 PM
     */
    public static Long getCurrentCompanyId() {
        return get(ContextConstants.CURRENT_COMPANY_ID_HEADER, Long.class);
    }

    public static void setCurrentCompanyId(Object val) {
        set(ContextConstants.CURRENT_COMPANY_ID_HEADER, val);
    }

    /**
     * 获取 当前所属的顶级公司ID
     *
     * @return java.lang.Long
     * @create [2022/9/9 4:50 PM ] [henhen6] [初始创建]
     * @since 2022/9/9 4:50 PM
     */
    public static Long getCurrentTopCompanyId() {
        return get(ContextConstants.CURRENT_TOP_COMPANY_ID_HEADER, Long.class);
    }

    public static void setCurrentTopCompanyId(Object val) {
        set(ContextConstants.CURRENT_TOP_COMPANY_ID_HEADER, val);
    }

    /**
     * 获取 当前所属的部门ID
     *
     * @return java.lang.Long
     * @create [2022/9/9 4:50 PM ] [henhen6] [初始创建]
     * @since 2022/9/9 4:50 PM
     */
    public static Long getCurrentDeptId() {
        return get(ContextConstants.CURRENT_DEPT_ID_HEADER, Long.class);
    }

    public static void setCurrentDeptId(Object val) {
        set(ContextConstants.CURRENT_DEPT_ID_HEADER, val);
    }

    public static void setTopCompanyIsAdmin(Boolean val) {
        set(ContextConstants.CURRENT_TOP_COMPANY_IS_ADMIN_HEADER, val);
    }

    public static boolean getTopCompanyIsAdmin() {
        return get(ContextConstants.CURRENT_TOP_COMPANY_IS_ADMIN_HEADER, Boolean.class);
    }

    private static boolean isEmptyLong(String key) {
        String val = getLocalMap().get(key);
        return StrUtil.isEmpty(val) || StrPool.NULL.equals(val) || StrPool.ZERO.equals(val);
    }

    private static boolean isEmptyStr(String key) {
        String val = getLocalMap().get(key);
        return val == null || StrPool.NULL.equals(val);
    }

    public static String getLogTraceId() {
        return get(ContextConstants.TRACE_ID_HEADER, String.class);
    }

    public static void setLogTraceId(String val) {
        set(ContextConstants.TRACE_ID_HEADER, val);
    }


    /**
     * 获取灰度版本号
     *
     * @return 灰度版本号
     */
    public static String getGrayVersion() {
        return get(ContextConstants.GRAY_VERSION, String.class);
    }

    public static void setGrayVersion(String val) {
        set(ContextConstants.GRAY_VERSION, val);
    }

    /**
     * 仅用于演示环境禁止执行某些操作
     * 后续sql是否可以执行
     */
    public static Boolean isProceed() {
        String proceed = get(ContextConstants.PROCEED, String.class);
        return StrUtil.isNotEmpty(proceed);
    }


    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
