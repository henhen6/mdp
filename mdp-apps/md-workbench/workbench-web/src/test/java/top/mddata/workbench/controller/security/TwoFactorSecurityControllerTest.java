package top.mddata.workbench.controller.security;

import org.junit.jupiter.api.Test;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.workbench.dto.TwoFactorChannelDto;
import top.mddata.workbench.dto.TwoFactorDisableDto;
import top.mddata.workbench.dto.TwoFactorEnableDto;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TwoFactorSecurityController} 操作日志注解守护。
 *
 * @author tangyh
 */
class TwoFactorSecurityControllerTest {

    @Test
    void 安全设置写操作保留修改类审计() throws NoSuchMethodException {
        assertUpdateLog("enable", TwoFactorEnableDto.class);
        assertUpdateLog("disable", TwoFactorDisableDto.class);
        assertUpdateLog("changeChannel", TwoFactorEnableDto.class);
    }

    @Test
    void 发送管理验证码使用独立渠道入参() throws NoSuchMethodException {
        Method method = TwoFactorSecurityController.class
                .getDeclaredMethod("sendEnableCode", TwoFactorChannelDto.class);

        assertEquals(1, method.getParameterCount(), "sendEnableCode 必须使用不含验证码的 TwoFactorChannelDto");
    }

    private void assertUpdateLog(String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        RequestLog requestLog = requestLogOf(methodName, parameterTypes);
        assertTrue(requestLog.enabled(), "操作审计必须保留");
        assertEquals(RequestLog.LogType.UPDATE, requestLog.logType(), "开通/关闭/换渠道属于修改类操作");
        assertFalse(requestLog.request(), "入参含验证码，禁止记录请求");
        assertFalse(requestLog.response(), "响应不含敏感信息也保持不记录，与解绑接口对齐");
        assertFalse(requestLog.requestByError(), "异常时也禁止记录含验证码的请求");
    }

    private RequestLog requestLogOf(String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = TwoFactorSecurityController.class.getDeclaredMethod(methodName, parameterTypes);
        return method.getAnnotation(RequestLog.class);
    }
}
