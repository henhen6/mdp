package top.mddata.base.apiperm.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

/**
 * 用户的接口放行集。
 *
 * <p>operationsAdmin=true 表示运营者（绑定启用中 OPERATIONS_ADMIN 角色），
 * 豁免一切接口权限，patterns 此时为空集。</p>
 */
@Data
@AllArgsConstructor
public class UserApiPerm {
    private boolean operationsAdmin;
    private Set<ApiPattern> patterns;
}
