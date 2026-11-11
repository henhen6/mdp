package top.mddata.workbench.controller;

import org.junit.jupiter.api.Test;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.workbench.dto.SocialBindExistingDto;
import top.mddata.workbench.dto.SocialRegisterDto;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SocialOnboardingController} 操作日志注解守护。
 *
 * <p>入参含密码与待选择票据，响应含访问令牌，正常与异常路径均禁止写入操作日志；
 * 审计本身保留（enabled=true）。</p>
 *
 * @author tangyh
 */
class SocialOnboardingControllerTest {

    @Test
    void 绑定已有账号接口保留审计但不记录请求响应和异常请求() throws NoSuchMethodException {
        RequestLog requestLog = requestLogOf("bindExisting", SocialBindExistingDto.class);

        assertTrue(requestLog.enabled(), "操作审计必须保留");
        assertFalse(requestLog.request(), "密码和待选择票据禁止记录请求");
        assertFalse(requestLog.response(), "登录响应含访问令牌，禁止记录响应");
        assertFalse(requestLog.requestByError(), "异常时也禁止记录含密码和票据的请求");
    }

    @Test
    void 社交注册接口保留审计但不记录请求响应和异常请求() throws NoSuchMethodException {
        RequestLog requestLog = requestLogOf("register", SocialRegisterDto.class);

        assertTrue(requestLog.enabled(), "操作审计必须保留");
        assertFalse(requestLog.request(), "待选择票据禁止记录请求");
        assertFalse(requestLog.response(), "登录响应含访问令牌，禁止记录响应");
        assertFalse(requestLog.requestByError(), "异常时也禁止记录含票据的请求");
    }

    private RequestLog requestLogOf(String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = SocialOnboardingController.class
                .getDeclaredMethod(methodName, parameterTypes);
        return method.getAnnotation(RequestLog.class);
    }
}
