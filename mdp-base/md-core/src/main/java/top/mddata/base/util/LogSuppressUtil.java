package top.mddata.base.util;

/**
 * 日志落库链路自身输出的抑制开关（线程级）。
 * <p>
 * 记录操作日志的异步链路（SysLogListener → 请求/响应日志入库）会调用 Service 与 Mapper，
 * 反过来又被方法日志切面、SQL 审计采集器拦截，导致每次请求都产生大量"关于日志的日志"自我放大。
 * 监听入口打标后，切面与审计输出前检查该标记即可跳过本链路的日志；日志入库动作本身不受影响，只过滤输出。
 * <p>
 * 异步线程池会复用线程，使用方必须在 finally 中 release()，防止标记泄漏给同线程的下一个任务。
 */
public final class LogSuppressUtil {

    private static final ThreadLocal<Boolean> SUPPRESSED = new ThreadLocal<>();

    /** 工具类，禁止实例化 */
    private LogSuppressUtil() {
    }

    /** 在当前线程开启抑制。由日志落库链路入口（如 SysLogListener）调用，必须在 finally 中配对 release() */
    public static void suppress() {
        SUPPRESSED.set(Boolean.TRUE);
    }

    /** 释放当前线程的抑制标记。异步线程池会复用线程，不 release 会泄漏给下一个任务 */
    public static void release() {
        SUPPRESSED.remove();
    }

    /** 当前线程是否处于抑制状态。方法日志切面、SQL 审计采集器输出前检查，为 true 时跳过打印 */
    public static boolean isSuppressed() {
        return Boolean.TRUE.equals(SUPPRESSED.get());
    }
}
