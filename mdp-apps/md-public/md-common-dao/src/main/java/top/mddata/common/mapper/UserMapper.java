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
     * <p>手写 SQL，已手动过滤 deleted_at = 0。</p>
     *
     * @param startTime 开始日期时间（包含），格式：yyyy-MM-dd 00:00:00
     * @param endTime   截止日期时间（包含），格式：yyyy-MM-dd 23:59:59
     * @return 每日新增用户数，key=date(yyyy-MM-dd)、value=count
     */
    @Select({
            """
            SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS date, COUNT(*) AS value
              FROM mdc_user
             WHERE deleted_at = 0
               AND created_at >= #{startTime, jdbcType=TIMESTAMP}
               AND created_at <= #{endTime, jdbcType=TIMESTAMP}
             GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d')
             ORDER BY date ASC
            """
    })
    List<Map<String, Object>> countByDayRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 按状态统计用户数。
     *
     * <p>手写 SQL，已手动过滤 deleted_at = 0。</p>
     *
     * @return 状态分布，key=state(1-正常/0-禁用)、name(展示名)、count
     */
    @Select({
            """
            SELECT
                state AS code,
                COUNT(*) AS count
              FROM mdc_user
             WHERE deleted_at = 0
             GROUP BY state
            """
    })
    List<Map<String, Object>> countByState();

    /**
     * 按组织性质统计用户数。
     *
     * <p>手写 SQL，已手动过滤 u 和 o 表的 deleted_at = 0。
     * 用户属于多个组织时按组织分别计数。
     * 没有任何组织关系的用户不计入分布（分布总数可能小于用户总数）。</p>
     *
     * @return 组织性质分布，key=nature(1-总公司 90-开发者 99-运营)、count
     */
    @Select({
            """
            SELECT
                o.nature AS code,
                COUNT(*) AS count
              FROM mdc_user u
              JOIN mdc_user_org_rel r ON r.user_id = u.id
              JOIN mdc_org o ON o.id = r.org_id
             WHERE u.deleted_at = 0
               AND o.deleted_at = 0
               AND o.nature IS NOT NULL
             GROUP BY o.nature
            """
    })
    List<Map<String, Object>> countByNature();

    /**
     * 统计本月新增用户数。
     *
     * <p>手写 SQL，已手动过滤 deleted_at = 0。</p>
     *
     * @param startTime 本月开始时间
     * @param endTime   本月结束时间
     * @return 用户数量
     */
    @Select({
            """
            SELECT COUNT(*) AS value
              FROM mdc_user
             WHERE deleted_at = 0
               AND created_at >= #{startTime, jdbcType=TIMESTAMP}
               AND created_at <= #{endTime, jdbcType=TIMESTAMP}
            """
    })
    Long countNewUsersInMonth(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
