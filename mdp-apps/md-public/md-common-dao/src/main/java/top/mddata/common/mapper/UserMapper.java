package top.mddata.common.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.common.entity.User;
import top.mddata.common.entity.base.UserBase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户 映射层。
 *
 * @author henhen6
 * @since 2025-11-12 15:44:52
 */
@Repository
public interface UserMapper extends SuperMapper<User> {
    /**
     * 重置 密码错误次数
     *
     * @param id  用户id
     * @param now 当前时间
     */
    @Update({
            """
                    update
                    """
            + UserBase.TABLE_NAME +
            """
                        set pw_error_num       = 0, pw_error_last_time = null, last_login_time          = #{now, jdbcType=TIMESTAMP}
                     where id = #{id, jdbcType=BIGINT}
                    """
    })
    void resetPwErrorNum(@Param("id") Long id, @Param("now") LocalDateTime now);

    /**
     * 递增 密码错误次数
     *
     * @param id  用户id
     * @param now 当前时间
     */
    @Update({
            """
                    update
                    """
            + UserBase.TABLE_NAME +
            """
                        set pw_error_num       = pw_error_num + 1, pw_error_last_time = #{now, jdbcType=TIMESTAMP}
                        where id = #{id, jdbcType=BIGINT}
                    """})
    void incrPwErrorNumById(@Param("id") Long id, @Param("now") LocalDateTime now);

    /**
     * 按日统计新增用户数（指定日期区间）。
     *
     * <p>手写 SQL，已手动过滤 deleted_at = 0。
     * treePathPrefix 不为空时，仅统计所属组织在该 tree_path 前缀子树内的用户。</p>
     *
     * @param startTime      开始日期时间（包含），格式：yyyy-MM-dd 00:00:00，null 表示不限
     * @param endTime        截止日期时间（包含），格式：yyyy-MM-dd 23:59:59，null 表示不限
     * @param treePathPrefix 组织树路径前缀（如 /123/%），null 表示全量
     * @return 每日新增用户数，key=date(yyyy-MM-dd)、value=count
     */
    @Select({
            """
                    <script>
                    SELECT DATE_FORMAT(u.created_at, '%Y-%m-%d') AS date, COUNT(*) AS value
                      FROM mdc_user u
                     WHERE u.deleted_at = 0
                       <if test="startTime != null">
                       AND u.created_at >= #{startTime, jdbcType=TIMESTAMP}
                       </if>
                       <if test="endTime != null">
                       AND u.created_at &lt;= #{endTime, jdbcType=TIMESTAMP}
                       </if>
                       <if test="treePathPrefix != null">
                       AND EXISTS (
                             SELECT 1 FROM mdc_user_org_rel r
                               JOIN mdc_org o ON o.id = r.org_id AND o.deleted_at = 0
                              WHERE r.user_id = u.id
                                AND o.tree_path LIKE #{treePathPrefix, jdbcType=VARCHAR})
                       </if>
                     GROUP BY DATE_FORMAT(u.created_at, '%Y-%m-%d')
                     ORDER BY date ASC
                    </script>
                    """
    })
    List<Map<String, Object>> countByDayRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
                                              @Param("treePathPrefix") String treePathPrefix);

    /**
     * 按日 + 组织性质统计新增用户数（指定日期区间）。
     *
     * <p>同一 (date, nature) 分组内按用户去重计数，与 countByNature 同口径；
     * 用户跨不同性质的多个组织时，分别计入各性质。</p>
     *
     * @param startTime 开始日期时间（包含），null 表示不限
     * @param endTime   截止日期时间（包含），null 表示不限
     * @return 每日各性质新增用户数，key=date(yyyy-MM-dd)、nature、value=count
     */
    @Select({
            """
                    <script>
                    SELECT DATE_FORMAT(u.created_at, '%Y-%m-%d') AS date, o.nature AS nature, COUNT(DISTINCT u.id) AS value
                      FROM mdc_user u
                      JOIN mdc_user_org_rel r ON r.user_id = u.id
                      JOIN mdc_org o ON o.id = r.org_id AND o.deleted_at = 0 AND o.nature IS NOT NULL
                     WHERE u.deleted_at = 0
                       <if test="startTime != null">
                       AND u.created_at >= #{startTime, jdbcType=TIMESTAMP}
                       </if>
                       <if test="endTime != null">
                       AND u.created_at &lt;= #{endTime, jdbcType=TIMESTAMP}
                       </if>
                     GROUP BY DATE_FORMAT(u.created_at, '%Y-%m-%d'), o.nature
                     ORDER BY date ASC
                    </script>
                    """
    })
    List<Map<String, Object>> countByDayRangeGroupByNature(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计正常状态的用户总数（概览卡片）。
     *
     * @param treePathPrefix 组织树路径前缀（如 /123/%），null 表示全量
     * @return 用户数量
     */
    @Select({
            """
                    <script>
                    SELECT COUNT(*) AS value
                      FROM mdc_user u
                     WHERE u.deleted_at = 0
                       <if test="treePathPrefix != null">
                       AND EXISTS (
                             SELECT 1 FROM mdc_user_org_rel r
                               JOIN mdc_org o ON o.id = r.org_id AND o.deleted_at = 0
                              WHERE r.user_id = u.id
                                AND o.tree_path LIKE #{treePathPrefix, jdbcType=VARCHAR})
                       </if>
                    </script>
                    """
    })
    Long countUsersInScope(@Param("treePathPrefix") String treePathPrefix);

    /**
     * 按状态统计用户数。
     *
     * <p>手写 SQL，已手动过滤 deleted_at = 0。</p>
     *
     * @param startTime      开始日期时间（包含），null 表示不限
     * @param endTime        截止日期时间（包含），null 表示不限
     * @param treePathPrefix 组织树路径前缀（如 /123/%），null 表示全量
     * @return 状态分布，key=state(1-正常/0-禁用)、name(展示名)、count
     */
    @Select({
            """
                    <script>
                    SELECT
                        u.state AS code,
                        COUNT(*) AS count
                      FROM mdc_user u
                     WHERE u.deleted_at = 0
                       <if test="startTime != null">
                       AND u.created_at >= #{startTime, jdbcType=TIMESTAMP}
                       </if>
                       <if test="endTime != null">
                       AND u.created_at &lt;= #{endTime, jdbcType=TIMESTAMP}
                       </if>
                       <if test="treePathPrefix != null">
                       AND EXISTS (
                             SELECT 1 FROM mdc_user_org_rel r
                               JOIN mdc_org o ON o.id = r.org_id AND o.deleted_at = 0
                              WHERE r.user_id = u.id
                                AND o.tree_path LIKE #{treePathPrefix, jdbcType=VARCHAR})
                       </if>
                     GROUP BY u.state
                    </script>
                    """
    })
    List<Map<String, Object>> countByState(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
                                           @Param("treePathPrefix") String treePathPrefix);

    /**
     * 按性别统计指定日期区间内的新增用户数。
     *
     * @param startTime      开始日期时间（包含），null 表示不限
     * @param endTime        截止日期时间（包含），null 表示不限
     * @param treePathPrefix 组织树路径前缀（如 /123/%），null 表示全量
     * @return 性别分布，key=sex(0-男 1-女，null 归为未知)、count
     */
    @Select({
            """
                    <script>
                    SELECT
                        u.sex AS code,
                        COUNT(*) AS count
                      FROM mdc_user u
                     WHERE u.deleted_at = 0
                       <if test="startTime != null">
                       AND u.created_at >= #{startTime, jdbcType=TIMESTAMP}
                       </if>
                       <if test="endTime != null">
                       AND u.created_at &lt;= #{endTime, jdbcType=TIMESTAMP}
                       </if>
                       <if test="treePathPrefix != null">
                       AND EXISTS (
                             SELECT 1 FROM mdc_user_org_rel r
                               JOIN mdc_org o ON o.id = r.org_id AND o.deleted_at = 0
                              WHERE r.user_id = u.id
                                AND o.tree_path LIKE #{treePathPrefix, jdbcType=VARCHAR})
                       </if>
                     GROUP BY u.sex
                    </script>
                    """
    })
    List<Map<String, Object>> countBySex(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime,
                                         @Param("treePathPrefix") String treePathPrefix);

    /**
     * 按组织性质统计用户数。
     *
     * <p>手写 SQL，已手动过滤 u 和 o 表的 deleted_at = 0。
     * 同一性质内按用户去重计数（避免用户同时挂在公司与部门被重复计入）；
     * 用户跨不同性质的多个组织时，分别计入各性质。
     * 没有任何组织关系的用户不计入分布（分布总数可能小于用户总数）。</p>
     *
     * @param startTime 开始日期时间（包含），null 表示不限
     * @param endTime   截止日期时间（包含），null 表示不限
     * @return 组织性质分布，key=nature(1-总公司 90-开发者 99-运营)、count
     */
    @Select({
            """
                    <script>
                    SELECT
                        o.nature AS code,
                        COUNT(DISTINCT u.id) AS count
                      FROM mdc_user u
                      JOIN mdc_user_org_rel r ON r.user_id = u.id
                      JOIN mdc_org o ON o.id = r.org_id
                     WHERE u.deleted_at = 0
                       AND o.deleted_at = 0
                       AND o.nature IS NOT NULL
                       <if test="startTime != null">
                       AND u.created_at >= #{startTime, jdbcType=TIMESTAMP}
                       </if>
                       <if test="endTime != null">
                       AND u.created_at &lt;= #{endTime, jdbcType=TIMESTAMP}
                       </if>
                     GROUP BY o.nature
                    </script>
                    """
    })
    List<Map<String, Object>> countByNature(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计本月新增用户数。
     *
     * <p>手写 SQL，已手动过滤 deleted_at = 0。</p>
     *
     * @param startTime 本月开始时间，null 表示不限
     * @param endTime   本月结束时间，null 表示不限
     * @return 用户数量
     */
    @Select({
            """
                    <script>
                    SELECT COUNT(*) AS value
                      FROM mdc_user
                     WHERE deleted_at = 0
                       <if test="startTime != null">
                       AND created_at >= #{startTime, jdbcType=TIMESTAMP}
                       </if>
                       <if test="endTime != null">
                       AND created_at &lt;= #{endTime, jdbcType=TIMESTAMP}
                       </if>
                    </script>
                    """
    })
    Long countNewUsersInMonth(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
