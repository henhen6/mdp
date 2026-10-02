package top.mddata.common.fieldperm;

import org.junit.jupiter.api.Test;
import top.mddata.base.fieldperm.model.FieldRule;
import top.mddata.base.fieldperm.model.UserFieldPerm;
import top.mddata.common.apiperm.ApiPermProviderImpl.RoleRow;
import top.mddata.common.fieldperm.FieldPermProviderImpl.ApiRow;
import top.mddata.common.fieldperm.FieldPermProviderImpl.FieldRow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldPermLoadLogicTest {

    @Test
    void 运营者短路_不查字段表() {
        List<RoleRow> roles = List.of(new RoleRow(1L, "OPERATIONS_ADMIN"));
        UserFieldPerm perm = FieldPermProviderImpl.assemble(roles, q -> {
            throw new IllegalStateException("运营者不应触发字段查询");
        });
        assertTrue(perm.isOperationsAdmin());
    }

    @Test
    void 无角色用户受限集为空() {
        UserFieldPerm perm = FieldPermProviderImpl.assemble(List.of(), q -> List.of());
        assertTrue(perm.getMenuRules().isEmpty());
    }

    @Test
    void 按菜单分组受限字段规则() {
        List<RoleRow> roles = List.of(new RoleRow(1L, "COMMON"));
        List<FieldRow> fields = List.of(
                new FieldRow(100L, "phone", FieldRule.RULE_TYPE_MASK, "mobile"),
                new FieldRow(100L, "idCard", FieldRule.RULE_TYPE_HIDE, null),
                new FieldRow(200L, "salary", FieldRule.RULE_TYPE_HIDE, null));
        UserFieldPerm perm = FieldPermProviderImpl.assemble(roles, q -> fields);

        assertEquals(2, perm.getMenuRules().size());
        assertEquals(2, perm.rulesOf(100L).size());
        assertTrue(perm.rulesOf(100L).get("phone").isMask());
        assertTrue(perm.rulesOf(100L).get("idCard").isHide());
        assertTrue(perm.rulesOf(200L).get("salary").isHide());
        // 未受限的菜单返回 null（调用方据此放行）
        assertNull(perm.rulesOf(300L));
    }

    @Test
    void uri菜单预解析_按钮沿上级链找字段规则菜单() {
        // 菜单树：100(菜单,有字段规则) - 101(按钮)；200(菜单,无规则) - 201(按钮)
        Map<Long, Long> parentOf = Map.of(101L, 100L, 201L, 200L);
        Set<Long> fieldMenuIds = Set.of(100L);
        List<ApiRow> apis = List.of(
                new ApiRow("/organization/user/page", "POST", 100L),
                new ApiRow("/organization/user/getById", "GET", 101L),
                new ApiRow("/organization/role/page", "POST", 200L));

        Map<String, Long> result = FieldPermProviderImpl.resolveUriMenu(apis, parentOf, fieldMenuIds);

        assertEquals(100L, result.get("POST /organization/user/page"));
        // getById 挂在按钮上，沿上级链解析到 100
        assertEquals(100L, result.get("GET /organization/user/getById"));
        // 无字段规则的菜单不进入映射（鉴权侧查空即放行）
        assertNull(result.get("POST /organization/role/page"));
    }

    @Test
    void uri菜单预解析_根菜单parent为null时链正常终止() {
        // 根菜单 parent_id 为 NULL：解析到根即停，不抛异常
        Map<Long, Long> parentOf = new HashMap<>();
        parentOf.put(100L, null);
        parentOf.put(101L, 100L);
        Map<String, Long> result = FieldPermProviderImpl.resolveUriMenu(
                List.of(new ApiRow("/a/b", "GET", 101L)), parentOf, Set.of(100L));
        assertEquals(100L, result.get("GET /a/b"));

        // 上级链无任何字段规则菜单时返回空
        Map<String, Long> empty = FieldPermProviderImpl.resolveUriMenu(
                List.of(new ApiRow("/a/b", "GET", 101L)), parentOf, Set.of(999L));
        assertTrue(empty.isEmpty());
    }

    @Test
    void uri菜单预解析_上级链成环不死循环() {
        // 脏数据：1 ↔ 2 互为父级
        Map<Long, Long> parentOf = Map.of(1L, 2L, 2L, 1L);
        Map<String, Long> result = FieldPermProviderImpl.resolveUriMenu(
                List.of(new ApiRow("/a/b", "GET", 1L)), parentOf, Set.of(99L));
        assertTrue(result.isEmpty());
    }
}
