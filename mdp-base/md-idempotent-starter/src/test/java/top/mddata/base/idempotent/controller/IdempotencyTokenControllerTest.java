package top.mddata.base.idempotent.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import top.mddata.base.idempotent.service.IdempotencyTokenService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/** 幂等令牌签发端点的路由契约。 */
class IdempotencyTokenControllerTest {

    @Test
    void tokenEndpointUsesAnyonePrefix() throws Exception {
        IdempotencyTokenService tokenService = mock(IdempotencyTokenService.class);
        when(tokenService.issue()).thenReturn("test-token");
        MockMvc mockMvc = standaloneSetup(new IdempotencyTokenController(tokenService)).build();

        mockMvc.perform(get("/anyone/idempotent/token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("test-token"));

        verify(tokenService).issue();
    }

    @Test
    void legacyTokenEndpointIsNotMapped() throws Exception {
        IdempotencyTokenService tokenService = mock(IdempotencyTokenService.class);
        MockMvc mockMvc = standaloneSetup(new IdempotencyTokenController(tokenService)).build();

        mockMvc.perform(get("/idempotent/token"))
                .andExpect(status().isNotFound());
    }
}
