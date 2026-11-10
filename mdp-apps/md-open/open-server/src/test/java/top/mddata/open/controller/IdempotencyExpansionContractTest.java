package top.mddata.open.controller;

import org.junit.jupiter.api.Test;
import top.mddata.base.annotation.idempotent.Idempotent;
import top.mddata.common.dto.IdDto;
import top.mddata.open.controller.client.ClientAppApplyController;
import top.mddata.open.controller.client.ClientAppScopeApplyController;
import top.mddata.open.dto.admin.AppApplyDto;
import top.mddata.open.dto.admin.AppScopeApplyDto;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** 第一批客户端写接口的幂等注解契约。 */
class IdempotencyExpansionContractTest {

    @Test
    void scopeApplyUsesIdempotent() throws NoSuchMethodException {
        Method method = ClientAppScopeApplyController.class
                .getDeclaredMethod("apply", AppScopeApplyDto.class);

        assertNotNull(method.getAnnotation(Idempotent.class));
    }

    @Test
    void appApplySubmitUsesIdempotent() throws NoSuchMethodException {
        Method method = ClientAppApplyController.class
                .getDeclaredMethod("submit", AppApplyDto.class);

        assertNotNull(method.getAnnotation(Idempotent.class));
    }

    @Test
    void otherClientActionsDoNotUseIdempotent() throws NoSuchMethodException {
        Method save = ClientAppApplyController.class
                .getDeclaredMethod("save", AppApplyDto.class);
        Method withdraw = ClientAppApplyController.class
                .getDeclaredMethod("withdraw", IdDto.class);

        assertNull(save.getAnnotation(Idempotent.class));
        assertNull(withdraw.getAnnotation(Idempotent.class));
    }
}
