package top.mddata.base.fieldperm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 用户字段受限集（拒绝模型：仅存放当前用户"受限"的字段规则）。
 *
 * <p>作为 Redis 缓存模型，必须保留无参构造供 Jackson 反序列化。</p>
 *
 * @author henhen6
 * @since 2026-10-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFieldPerm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否运营管理员（豁免一切字段限制）
     */
    private boolean operationsAdmin;

    /**
     * 受限字段集：menuId → property → 规则
     */
    private Map<Long, Map<String, FieldRule>> menuRules;

    /** 取指定菜单下当前用户的受限字段规则，无限制返回 null */
    public Map<String, FieldRule> rulesOf(Long menuId) {
        if (operationsAdmin || menuRules == null || menuId == null) {
            return null;
        }
        return menuRules.get(menuId);
    }
}
