package top.mddata.workbench.controller;

import org.junit.jupiter.api.Test;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.workbench.dto.SocialRegisterDto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 注册接口操作日志注解守护。
 *
 * @author tangyh
 */
class RegisterControllerTest {

    @Test
    void 社交注册接口保留审计但不记录请求响应和异常请求() throws NoSuchMethodException {
        RequestLog requestLog = RegisterController.class.getDeclaredMethod("register", SocialRegisterDto.class)
                .getAnnotation(RequestLog.class);

        assertNotNull(requestLog, "社交注册必须保留操作审计日志");
        assertFalse(requestLog.request(), "待选择票据禁止记录请求");
        assertFalse(requestLog.response(), "登录响应含访问令牌，禁止记录响应");
        assertFalse(requestLog.requestByError(), "异常时也禁止记录含票据的请求");
    }
}
