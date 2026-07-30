package top.mddata.console.mapper.permission;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.console.entity.permission.Role;

import java.util.List;
import java.util.Map;

/**
 * 角色 映射层。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Repository
public interface RoleMapper extends SuperMapper<Role> {

    /**
     * 按角色统计用户数量排行（角色用户排行）。
     *
     * <p>手写 SQL，已手动过滤 deleted_at = 0。</p>
     *
     * @param limit 排行榜上限
     * @return 角色用户排行，key=roleId、name(角色名)、value(用户数)
     */
    @Select({
            """
                    SELECT r.id AS roleId, r.name AS name, COUNT(urr.user_id) AS value
                      FROM mdc_role r
                      LEFT JOIN mdc_user_role_rel urr ON urr.role_id = r.id
                      LEFT JOIN mdc_user u ON u.id = urr.user_id AND u.deleted_at = 0
                     WHERE r.deleted_at = 0
                       AND r.state = 1
                     GROUP BY r.id, r.name
                     ORDER BY value DESC, r.created_at DESC
                     LIMIT #{limit}
                    """
    })
    List<Map<String, Object>> rankByUserCount(@Param("limit") int limit);
}
