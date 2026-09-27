package top.mddata.base.mybatisflex.datascope.context;

import top.mddata.base.mybatisflex.datascope.annotation.DataScope;

/**
 * 数据权限上下文（ThreadLocal）。
 *
 * <p>嵌套方法场景必须"进入时保存外层、退出时恢复外层"，
 * 禁止直接 remove，否则外层查询会被内层编码污染。</p>
 *
 * @author henhen
 * @since 2026年09月26日
 */
public final class DataScopeContext {

    private static final ThreadLocal<DataScope> THREAD_LOCAL = new ThreadLocal<>();

    private DataScopeContext() {
    }

    public static DataScope get() {
        return THREAD_LOCAL.get();
    }

    /**
     * 设置当前注解并返回被覆盖的外层值（可能为 null），供退出时 restore
     */
    public static DataScope setAndGetPrevious(DataScope dataScope) {
        DataScope previous = THREAD_LOCAL.get();
        THREAD_LOCAL.set(dataScope);
        return previous;
    }

    /**
     * 恢复外层值；外层为 null 时清理 ThreadLocal，避免线程复用泄漏
     */
    public static void restore(DataScope previous) {
        if (previous == null) {
            THREAD_LOCAL.remove();
        } else {
            THREAD_LOCAL.set(previous);
        }
    }
}
