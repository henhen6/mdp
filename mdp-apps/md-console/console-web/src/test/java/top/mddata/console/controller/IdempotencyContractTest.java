package top.mddata.console.controller;

import org.junit.jupiter.api.Test;
import top.mddata.base.annotation.idempotent.Idempotent;
import top.mddata.base.exception.code.ExceptionCode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IdempotencyContractTest {
    @Test
    void annotationIsAvailableAtRuntime() throws NoSuchMethodException {
        Method method = Fixture.class.getDeclaredMethod("save");
        assertNotNull(method.getAnnotation(Idempotent.class));
        assertEquals(RetentionPolicy.RUNTIME,
                Idempotent.class.getAnnotation(Retention.class).value());
    }

    @Test
    void errorCodesMatchFrontendProtocol() {
        assertEquals(-14, ExceptionCode.IDEMPOTENT_PROCESSING.getCode());
        assertEquals(-15, ExceptionCode.IDEMPOTENT_CONFLICT.getCode());
        assertEquals(-16, ExceptionCode.IDEMPOTENT_TOKEN_INVALID.getCode());
    }

    private static class Fixture {
        @Idempotent
        void save() {
        }
    }
}
