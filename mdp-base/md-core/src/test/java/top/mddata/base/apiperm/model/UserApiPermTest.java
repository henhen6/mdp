package top.mddata.base.apiperm.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 缓存模型的 JSON 往返测试：缓存经 Jackson2JsonRedisSerializer 读写，
 * 模型必须能被 Jackson 反序列化（需无参构造或显式 Creator）。
 */
class UserApiPermTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void jsonRoundTripShouldKeepFields() throws Exception {
        UserApiPerm source = new UserApiPerm(false,
                Set.of(new ApiPattern("/organization/user/page", "POST"),
                        new ApiPattern("/organization/user/*", "ALL")));

        String json = objectMapper.writeValueAsString(source);
        UserApiPerm restored = objectMapper.readValue(json, UserApiPerm.class);

        assertEquals(source.isOperationsAdmin(), restored.isOperationsAdmin());
        assertEquals(source.getPatterns(), restored.getPatterns());
    }

    @Test
    void jsonRoundTripForOperationsAdmin() throws Exception {
        UserApiPerm source = new UserApiPerm(true, Set.of());

        UserApiPerm restored = objectMapper.readValue(
                objectMapper.writeValueAsString(source), UserApiPerm.class);

        assertTrue(restored.isOperationsAdmin());
        assertTrue(restored.getPatterns().isEmpty());
    }
}
