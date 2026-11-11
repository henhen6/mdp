package top.mddata.workbench.controller;

import org.junit.jupiter.api.Test;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.workbench.dto.SocialCallbackDto;
import top.mddata.workbench.dto.SocialReauthDto;
import top.mddata.workbench.dto.SocialReauthUrlDto;
import top.mddata.workbench.dto.SocialTicketDto;
import top.mddata.workbench.dto.SocialTransferDto;
import top.mddata.workbench.dto.SocialUnbindDto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 个人中心第三方账号控制器日志守护：含凭据、票据、授权码和 state 的接口
 * 请求体、正常响应体与异常请求体都不得写入操作日志。
 *
 * @author tangyh
 */
class ProfileSocialControllerTest {

    @Test
    void 敏感接口保留审计但全量关闭请求与响应日志() throws Exception {
        assertAllSensitiveLogDisabled("reauth", SocialReauthDto.class);
        assertAllSensitiveLogDisabled("reauthUrl", String.class, SocialReauthUrlDto.class);
        assertAllSensitiveLogDisabled("reauthByCallback", SocialCallbackDto.class);
        assertAllSensitiveLogDisabled("bindUrl", String.class, SocialTicketDto.class);
        assertAllSensitiveLogDisabled("bind", SocialCallbackDto.class);
        assertAllSensitiveLogDisabled("unbind", SocialUnbindDto.class);
        assertAllSensitiveLogDisabled("transfer", SocialTransferDto.class);
    }

    private void assertAllSensitiveLogDisabled(String name, Class<?>... parameterTypes)
            throws Exception {
        RequestLog log = ProfileSocialController.class.getMethod(name, parameterTypes)
                .getAnnotation(RequestLog.class);
        assertNotNull(log, "接口 [" + name + "] 必须保留操作审计日志");
        assertFalse(log.request(), "接口 [" + name + "] 请求体含敏感信息，禁止记录");
        assertFalse(log.response(), "接口 [" + name + "] 响应体禁止记录");
        assertFalse(log.requestByError(), "接口 [" + name + "] 异常时禁止记录请求体");
    }
}