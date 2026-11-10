package top.mddata.console.controller;

import org.junit.jupiter.api.Test;
import top.mddata.base.annotation.idempotent.Idempotent;
import top.mddata.console.controller.organization.UserController;
import top.mddata.console.dto.organization.UserDto;
import top.mddata.console.dto.organization.UserUpdateDto;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserControllerIdempotencyTest {
    @Test
    void onlyUserCreationUsesIdempotency() throws NoSuchMethodException {
        Method save = UserController.class.getDeclaredMethod("save", UserDto.class);
        Method update = UserController.class.getDeclaredMethod("update", UserUpdateDto.class);

        assertNotNull(save.getAnnotation(Idempotent.class));
        assertNull(update.getAnnotation(Idempotent.class));
    }
}
