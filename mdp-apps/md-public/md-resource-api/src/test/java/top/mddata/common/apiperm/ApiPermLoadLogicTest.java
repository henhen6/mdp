package top.mddata.common.apiperm;

import org.junit.jupiter.api.Test;
import top.mddata.base.apiperm.model.ApiPattern;
import top.mddata.base.apiperm.model.UserApiPerm;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApiPermLoadLogicTest {

    @Test
    void 运营者短路_不查接口表() {
        List<ApiPermProviderImpl.RoleRow> roles = List.of(
                new ApiPermProviderImpl.RoleRow(1L, "OPERATIONS_ADMIN"));
        UserApiPerm perm = ApiPermProviderImpl.assemble(roles, q -> {
            throw new IllegalStateException("运营者不应触发接口查询");
        });
        assertTrue(perm.isOperationsAdmin());
        assertTrue(perm.getPatterns().isEmpty());
    }

    @Test
    void 普通用户_多角色接口并集去重() {
        List<ApiPermProviderImpl.RoleRow> roles = List.of(
                new ApiPermProviderImpl.RoleRow(1L, "FINANCE"),
                new ApiPermProviderImpl.RoleRow(2L, "USER"));
        UserApiPerm perm = ApiPermProviderImpl.assemble(roles, q ->
                List.of(new ApiPattern("/a/page", "POST"), new ApiPattern("/a/page", "POST"),
                        new ApiPattern("/a/**", "ALL")));
        assertFalse(perm.isOperationsAdmin());
        assertEquals(Set.of(new ApiPattern("/a/page", "POST"), new ApiPattern("/a/**", "ALL")),
                perm.getPatterns());
    }

    @Test
    void 无角色用户_空放行集() {
        UserApiPerm perm = ApiPermProviderImpl.assemble(List.of(), q -> List.of());
        assertFalse(perm.isOperationsAdmin());
        assertTrue(perm.getPatterns().isEmpty());
    }
}
