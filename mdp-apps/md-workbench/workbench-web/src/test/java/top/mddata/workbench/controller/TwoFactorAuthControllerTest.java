package top.mddata.workbench.controller;

import org.junit.jupiter.api.Test;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.workbench.dto.TwoFactorChannelDto;
import top.mddata.workbench.dto.TwoFactorDisableDto;
import top.mddata.workbench.dto.TwoFactorEnableDto;
import top.mddata.workbench.dto.TwoFactorVerifyDto;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TwoFactorAuthController} 操作日志注解守护。
 *
 * <p>verify 入参含一次性票据与验证码、出参含访问令牌，任何情况下都禁止写入操作日志；
 * 开通/关闭/换渠道属于修改类操作，审计必须保留，但其入参含 OTP 明文，
 * 请求/响应/异常请求均禁止落日志（对齐第三方账号解绑接口的敏感接口写法）。</p>
 *
 * @author tangyh
 */
class TwoFactorAuthControllerTest {

    @Test
    void 登录二次验证接口保留审计但不记录请求和响应() throws NoSuchMethodException {
        RequestLog requestLog = requestLogOf("verify", TwoFactorVerifyDto.class);

        assertTrue(requestLog.enabled(), "操作审计必须保留");
        assertFalse(requestLog.request(), "票据和验证码禁止记录请求");
        assertFalse(requestLog.response(), "登录响应含访问令牌，禁止记录响应");
        assertFalse(requestLog.requestByError(), "异常时也禁止记录含票据和验证码的请求");
    }

    @Test
    void 安全设置写操作保留修改类审计() throws NoSuchMethodException {
        assertUpdateLog("enable", TwoFactorEnableDto.class);
        assertUpdateLog("disable", TwoFactorDisableDto.class);
        assertUpdateLog("changeChannel", TwoFactorEnableDto.class);
    }

    @Test
    void 发送管理验证码使用独立渠道入参() throws NoSuchMethodException {
        Method method = TwoFactorAuthController.class
                .getDeclaredMethod("sendEnableCode", TwoFactorChannelDto.class);

        assertEquals(1, method.getParameterCount(),
                "sendEnableCode 必须使用不含验证码的 TwoFactorChannelDto");
    }

    private void assertUpdateLog(String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        RequestLog requestLog = requestLogOf(methodName, parameterTypes);
        assertTrue(requestLog.enabled(), "操作审计必须保留");
        assertEquals(RequestLog.LogType.UPDATE, requestLog.logType(),
                "开通/关闭/换渠道属于修改类操作");
        assertFalse(requestLog.request(), "入参含验证码，禁止记录请求");
        assertFalse(requestLog.response(), "响应不含敏感信息也保持不记录，与解绑接口对齐");
        assertFalse(requestLog.requestByError(), "异常时也禁止记录含验证码的请求");
    }

    private RequestLog requestLogOf(String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = TwoFactorAuthController.class
                .getDeclaredMethod(methodName, parameterTypes);
        return method.getAnnotation(RequestLog.class);
    }
}
