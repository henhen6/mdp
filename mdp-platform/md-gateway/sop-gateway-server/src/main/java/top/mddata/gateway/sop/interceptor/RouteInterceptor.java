package top.mddata.gateway.sop.interceptor;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import top.mddata.gateway.sop.common.RouteContext;

/**
 * 路由拦截器
 *
 * @author 六如
 */
public interface RouteInterceptor {

    /**
     * 初始化，spring容器启动后执行。
     *
     * @param applicationContext spring上下文
     * @param environment        spring环境变量
     */
    default void init(ApplicationContext applicationContext, Environment environment) {

    }

    /**
     * 在路由转发前执行，签名校验通过后会立即执行此方法
     * <pre>
     * 在这个方法中抛出异常会中断接口执行，直接返回错误信息
     * </pre>
     *
     * @param routeContext routeContext
     */
    default void preRoute(RouteContext routeContext) {
    }

    /**
     * 微服务返回结果后执行
     *
     * @param routeContext routeContext
     * @param result       业务返回结果,通常是HashMap
     * @return 返回格式化后的结果, 可对原结果进行修改
     */
    default Object afterRoute(RouteContext routeContext, Object result) {
        return result;
    }

    /**
     * 拦截器执行顺序，值小优先执行，建议从0开始，小于0留给系统使用
     *
     * @return 返回顺序
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 是否匹配，返回true执行拦截器，默认true
     *
     * @param routeContext routeContext
     * @return 返回true执行拦截器
     */
    default boolean match(RouteContext routeContext) {
        return true;
    }
}
