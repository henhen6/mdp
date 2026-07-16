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
     * 按日统计新增用户数（指定起始时间之后）。
     *
     * <p>手写 SQL，已手动过滤 deleted_at = 0。</p>
     *
     * @param startTime 起始时间（包含）
     * @return 每日新增用户数，key=date(yyyy-MM-dd)、value=count
     */
    @Select({
            """
            SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS date, COUNT(*) AS value
              FROM mdc_user
             WHERE deleted_at = 0
               AND created_at >= #{startTime, jdbcType=TIMESTAMP}
             GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d')
             ORDER BY date ASC
            """
    })
    List<Map<String, Object>> countByDay(@Param("startTime") LocalDateTime startTime);

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
                CASE WHEN state = 1 THEN '正常' ELSE '禁用' END AS name,
                state AS state,
                COUNT(*) AS count
              FROM mdc_user
             WHERE deleted_at = 0
             GROUP BY state
            """
    })
    List<Map<String, Object>> countByState();

    /**
     * 按人员类型统计用户数。
     *
     * <p>手写 SQL，已手动过滤 deleted_at = 0。</p>
     *
     * @return 人员类型分布，key=userType、name(展示名)、count
     */
    @Select({
            """
            SELECT
                user_type AS userType,
                CASE user_type
                    WHEN 1 THEN '普通用户'
                    WHEN 2 THEN '管理员'
                    WHEN 99 THEN '运维管理员'
                    ELSE '其他'
                END AS name,
                COUNT(*) AS count
              FROM mdc_user
             WHERE deleted_at = 0
             GROUP BY user_type
            """
    })
    List<Map<String, Object>> countByType();
}
