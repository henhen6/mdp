package top.mddata.common.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.common.entity.Org;

import java.util.List;
import java.util.Map;

/**
 * 组织 映射层。
 *
 * @author henhen6
 * @since 2025-11-12 15:49:10
 */
@Repository
public interface OrgMapper extends SuperMapper<Org> {
    /**
     * 查询用户拥有的机构
     *
     * @param userId 用户id
     * @return java.util.List<java.lang.Long>
     */
    @Select({"""
             SELECT DISTINCT r.id
                    FROM mdc_org r INNER JOIN mdc_user_org_rel our on r.id = our.org_id
                    where  our.user_id = #{userId} and r.state = 1
            """})
    List<Long> selectOrgByUserId(@Param("userId") Long userId);

    /**
     * 按组织统计用户数量排行（部门用户排行）。
     *
     * <p>手写 SQL，已手动过滤 deleted_at = 0。</p>
     *
     * @param treePathPrefix 组织树路径前缀（如 /123/%），null 表示全量
     * @param limit          排行榜上限
     * @return 组织用户排行，key=orgId、name(组织名)、value(用户数)
     */
    @Select({
            """
                    <script>
                    SELECT o.id AS orgId, o.name AS name, COUNT(our.user_id) AS value
                      FROM mdc_org o
                      LEFT JOIN mdc_user_org_rel our ON our.org_id = o.id
                      LEFT JOIN mdc_user u ON u.id = our.user_id AND u.deleted_at = 0
                     WHERE o.deleted_at = 0
                       AND o.state = 1
                       <if test="treePathPrefix != null">
                       AND o.tree_path LIKE #{treePathPrefix, jdbcType=VARCHAR}
                       </if>
                     GROUP BY o.id, o.name
                     ORDER BY value DESC, o.weight  DESC, o.created_at  DESC
                     LIMIT #{limit}
                    </script>
                    """
    })
    List<Map<String, Object>> rankByUserCount(@Param("treePathPrefix") String treePathPrefix, @Param("limit") int limit);
}
