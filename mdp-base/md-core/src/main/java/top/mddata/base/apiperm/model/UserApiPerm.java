package top.mddata.base.apiperm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 用户的接口放行集。
 *
 * <p>operationsAdmin=true 表示运营者（绑定启用中 OPERATIONS_ADMIN 角色），
 * 豁免一切接口权限，patterns 此时为空集。</p>
 *
 * <p>作为 Redis 缓存模型，必须保留无参构造供 Jackson 反序列化。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserApiPerm {
    private boolean operationsAdmin;
    private Set<ApiPattern> patterns;
}
