package top.mddata.workbench.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.workbench.entity.LoginLog;

import java.util.List;
import java.util.Map;

/**
 * 登录日志 映射层。
 *
 * @author henhen6
 * @since 2025-11-12 23:46:53
 */
@Repository
public interface LoginLogMapper extends SuperMapper<LoginLog> {

    /**
     * 按省份统计登录次数（用于地域分布地图与排行榜）。
     *
     * <p>手写 SQL，mdw_login_log 表无逻辑删除字段。
     * ip_province 为空时不计入。</p>
     *
     * @param loginDate 登录日期 (yyyy-MM-dd)
     * @param limit 排行榜上限，地域分布传 NULL 或 0 表示全量
     * @return 省份-次数分布
     */
    @Select({
            """
            SELECT ip_province AS name, COUNT(*) AS value
              FROM mdw_login_log
             WHERE ip_province IS NOT NULL
               AND ip_province <> ''
             GROUP BY ip_province
             ORDER BY value DESC
             LIMIT #{limit}
            """
    })
    List<Map<String, Object>> rankByProvince(@Param("loginDate") String loginDate, @Param("limit") int limit);

    /**
     * 按登录IP统计次数排行。
     *
     * <p>手写 SQL，mdw_login_log 表无逻辑删除字段。</p>
     *
     * @param loginDate 登录日期 (yyyy-MM-dd)
     * @param limit 排行榜上限
     * @return IP-次数排行
     */
    @Select({
            """
            SELECT login_ip AS name, COUNT(*) AS value
              FROM mdw_login_log
             WHERE login_ip IS NOT NULL
               AND login_ip <> ''
               AND login_date = #{loginDate}
             GROUP BY login_ip
             ORDER BY value DESC
             LIMIT #{limit}
            """
    })
    List<Map<String, Object>> rankByIp(@Param("loginDate") String loginDate, @Param("limit") int limit);

    /**
     * 按姓名统计登录次数排行。
     *
     * <p>手写 SQL，mdw_login_log 表无逻辑删除字段。</p>
     *
     * @param loginDate 登录日期 (yyyy-MM-dd)
     * @param limit 排行榜上限
     * @return 姓名-次数排行
     */
    @Select({
            """
            SELECT name AS name, COUNT(*) AS value
              FROM mdw_login_log
             WHERE name IS NOT NULL
               AND name <> ''
               AND login_date = #{loginDate}
             GROUP BY name
             ORDER BY value DESC
             LIMIT #{limit}
            """
    })
    List<Map<String, Object>> rankByName(@Param("loginDate") String loginDate, @Param("limit") int limit);

    /**
     * 按浏览器名称统计。
     */
    @Select({
            """
            SELECT browser_name AS name, COUNT(*) AS count
              FROM mdw_login_log
             WHERE browser_name IS NOT NULL
               AND browser_name <> ''
             GROUP BY browser_name
             ORDER BY count DESC
            """
    })
    List<Map<String, Object>> countByBrowser();

    /**
     * 按操作系统统计。
     */
    @Select({
            """
            SELECT os AS name, COUNT(*) AS count
              FROM mdw_login_log
             WHERE os IS NOT NULL
               AND os <> ''
             GROUP BY os
             ORDER BY count DESC
            """
    })
    List<Map<String, Object>> countByOs();

    /**
     * 按登录方式统计。
     */
    @Select({
            """
            SELECT auth_type AS code,
                   CASE auth_type
                       WHEN '01' THEN '用户名密码验证码登录'
                       WHEN '02' THEN '用户名密码登录'
                       WHEN '03' THEN '手机短信验证码'
                       WHEN '04' THEN '邮箱验证码登录'
                       ELSE auth_type
                   END AS name,
                   COUNT(*) AS count
              FROM mdw_login_log
             WHERE auth_type IS NOT NULL
               AND auth_type <> ''
             GROUP BY auth_type
             ORDER BY count DESC
            """
    })
    List<Map<String, Object>> countByAuthType();

    /**
     * 按登录渠道统计。
     */
    @Select({
            """
            SELECT login_channel AS code,
                   CASE login_channel
                       WHEN '01' THEN '系统登录页'
                       WHEN '02' THEN '移动端'
                       ELSE login_channel
                   END AS name,
                   COUNT(*) AS count
              FROM mdw_login_log
             WHERE login_channel IS NOT NULL
               AND login_channel <> ''
             GROUP BY login_channel
             ORDER BY count DESC
            """
    })
    List<Map<String, Object>> countByChannel();

    /**
     * 按事件类型统计。
     */
    @Select({
            """
            SELECT event_type AS code,
                   CASE event_type
                       WHEN '01' THEN '登录'
                       WHEN '02' THEN '退出'
                       WHEN '03' THEN '注销'
                       WHEN '04' THEN '切换'
                       WHEN '05' THEN '扮演'
                       ELSE event_type
                   END AS name,
                   COUNT(*) AS count
              FROM mdw_login_log
             WHERE event_type IS NOT NULL
               AND event_type <> ''
             GROUP BY event_type
             ORDER BY count DESC
            """
    })
    List<Map<String, Object>> countByEventType();

    /**
     * 按日统计登录次数与登录人次（指定起始日期之后）。
     *
     * <p>登录次数：login_date = 当天 且 status = 01（成功） 的全部条数。
     * 登录人次：login_date = 当天 且 status = 01（成功） 的去重用户数。</p>
     */
    @Select({
            """
            SELECT login_date AS date,
                   COUNT(*) AS loginCount,
                   COUNT(DISTINCT user_id) AS userCount
              FROM mdw_login_log
             WHERE login_date >= #{startDate}
               AND status = '01'
             GROUP BY login_date
             ORDER BY date ASC
            """
    })
    List<Map<String, Object>> dailyStatistics(@Param("startDate") String startDate);

    /**
     * 最近7天活跃用户排行。
     */
    @Select({
            """
            SELECT name AS name, COUNT(*) AS value
              FROM mdw_login_log
             WHERE name IS NOT NULL
               AND name <> ''
               AND login_date >= #{startDate}
               AND status = '01'
             GROUP BY name
             ORDER BY value DESC
             LIMIT #{limit}
            """
    })
    List<Map<String, Object>> activeUserRank(@Param("startDate") String startDate, @Param("limit") int limit);

    /**
     * 按小时统计登录次数（指定日期）。
     *
     * <p>login_date 是 yyyy-MM-dd 的字符串（无小时信息），需要从 created_at (datetime(3)) 中提取小时。
     * 由于 LoginLog 没有显式 created_at 字段但继承了 BaseEntity，使用 created_at 列。</p>
     */
    @Select({
            """
            SELECT HOUR(created_at) AS hour, COUNT(*) AS count
              FROM mdw_login_log
             WHERE login_date = #{loginDate}
               AND status = '01'
             GROUP BY HOUR(created_at)
             ORDER BY hour ASC
            """
    })
    List<Map<String, Object>> hourlyDistribution(@Param("loginDate") String loginDate);
}
