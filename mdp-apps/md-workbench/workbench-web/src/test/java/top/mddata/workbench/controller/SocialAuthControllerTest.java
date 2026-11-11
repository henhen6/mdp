package top.mddata.workbench.controller;

import org.junit.jupiter.api.Test;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.workbench.dto.SocialCallbackDto;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SocialAuthController} 操作日志注解守护。
 *
 * <p>授权地址含 state 与 client_id，登录成功响应含访问令牌，两者均禁止写入操作日志；
 * 审计本身保留（enabled=true）。</p>
 *
 * @author tangyh
 */
class SocialAuthControllerTest {

    @Test
    void 获取授权地址接口保留审计但不记录响应() throws NoSuchMethodException {
        RequestLog requestLog = requestLogOf("loginUrl", String.class);

        assertTrue(requestLog.enabled(), "操作审计必须保留");
        assertFalse(requestLog.response(), "授权地址含state和client_id，禁止记录响应");
    }

    @Test
    void 第三方登录接口保留审计但不记录请求和响应() throws NoSuchMethodException {
        RequestLog requestLog = requestLogOf("login", SocialCallbackDto.class);

        assertTrue(requestLog.enabled(), "操作审计必须保留");
        assertFalse(requestLog.request(), "授权码和state禁止记录请求");
        assertFalse(requestLog.response(), "登录响应含访问令牌，禁止记录响应");
        assertFalse(requestLog.requestByError(), "异常时也禁止记录含授权码和state的请求");
    }

    private RequestLog requestLogOf(String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = SocialAuthController.class.getDeclaredMethod(methodName, parameterTypes);
        return method.getAnnotation(RequestLog.class);
    }
}
