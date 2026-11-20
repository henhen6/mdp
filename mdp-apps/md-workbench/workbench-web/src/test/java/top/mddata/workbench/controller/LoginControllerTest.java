package top.mddata.workbench.controller;

import org.junit.jupiter.api.Test;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.workbench.dto.SocialBindExistingDto;
import top.mddata.workbench.dto.SocialCallbackDto;
import top.mddata.workbench.dto.TwoFactorVerifyDto;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 登录接口操作日志注解守护，防止凭据、票据和访问令牌落日志。
 *
 * @author tangyh
 */
class LoginControllerTest {

    @Test
    void 获取授权地址接口保留审计但不记录响应() throws NoSuchMethodException {
        RequestLog requestLog = requestLogOf("loginUrl", String.class);

        assertTrue(requestLog.enabled(), "操作审计必须保留");
        assertFalse(requestLog.response(), "授权地址含state和client_id，禁止记录响应");
    }

    @Test
    void 敏感登录接口保留审计但不记录请求响应和异常请求() throws NoSuchMethodException {
        assertAllSensitiveLogDisabled("login", SocialCallbackDto.class);
        assertAllSensitiveLogDisabled("bindExisting", SocialBindExistingDto.class);
        assertAllSensitiveLogDisabled("verify", TwoFactorVerifyDto.class);
    }

    private void assertAllSensitiveLogDisabled(String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        RequestLog requestLog = requestLogOf(methodName, parameterTypes);
        assertNotNull(requestLog, "接口 [" + methodName + "] 必须保留操作审计日志");
        assertFalse(requestLog.request(), "接口 [" + methodName + "] 请求含敏感信息，禁止记录");
        assertFalse(requestLog.response(), "接口 [" + methodName + "] 响应可能含访问令牌，禁止记录");
        assertFalse(requestLog.requestByError(), "接口 [" + methodName + "] 异常时也禁止记录请求");
    }

    private RequestLog requestLogOf(String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = LoginController.class.getDeclaredMethod(methodName, parameterTypes);
        return method.getAnnotation(RequestLog.class);
    }
}
