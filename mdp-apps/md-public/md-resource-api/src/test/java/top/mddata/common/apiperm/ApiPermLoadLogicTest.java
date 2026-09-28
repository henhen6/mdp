package top.mddata.common.apiperm;

import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import top.mddata.base.apiperm.model.ApiPattern;
import top.mddata.base.apiperm.model.UserApiPerm;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void testToSql() {
        QueryWrapper wrapper = QueryWrapper.create()
                .select("r.id AS roleId", "r.code AS code")
                .from("mdc_user_role_rel").as("ur")
                .innerJoin("mdc_role").as("r").on("ur.role_id = r.id")
                .where("ur.user_id = ?", 1)
                .and("r.state = ?", Boolean.TRUE)
                .and("r.deleted_at = 0");
        System.out.println(wrapper.toSQL());


        List<Long> roleIds = List.of(1L, 2L, 3L);
        QueryWrapper wrapper1 = QueryWrapper.create()
                .select("DISTINCT resource_id AS resourceId")
                .from("mdc_role_resource_rel")
                .where("role_id IN (" + roleIds.stream().map(String::valueOf)
                        .collect(Collectors.joining(",")) + ")");
        System.out.println(wrapper1.toSQL());

        String ids = "1,2,3";
        QueryWrapper wrapper2 = QueryWrapper.create()
                .select("uri", "request_method AS requestMethod")
                .from("mdc_resource_api")
                .where("resource_id IN (" + ids + ")");
        System.out.println(wrapper2.toSQL());
    }
}
