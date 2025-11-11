package top.mddata.common.cache;

/**
 * 缓存模块
 *
 * @author henhen6
 * @since 2020/10/21
 */
public class CacheKeyModular {
    /**
     * 多个服务都会使用的缓存
     */
    public static final String COMMON = "common";

    /** 缓存key前缀， 可以在启动时覆盖该参数。
     * 系统启动时，注入。
     */
    public static String PREFIX;
}
