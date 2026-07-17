package top.mddata.console.mapper.message;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.console.entity.message.MsgTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息任务 映射层。
 *
 * @author henhen6
 * @since 2025-12-21 00:02:22
 */
@Repository
public interface MsgTaskMapper extends SuperMapper<MsgTask> {

    @Select("select * from mdc_msg_task where title = #{title}")
    List<MsgTask> listByTitle(@Param("title") String title);

    /**
     * 按消息类型统计（mdc_msg_task 没有 deleted_at 字段）。
     */
    @Select({
            """
            SELECT type AS code, COUNT(*) AS count
              FROM mdc_msg_task
             WHERE type IS NOT NULL
             GROUP BY type
             ORDER BY count DESC
            """
    })
    List<Map<String, Object>> countByType();

    /**
     * 按日统计成功发送数（指定起始时间之后）。
     */
    @Select({
            """
            SELECT DATE_FORMAT(send_time, '%Y-%m-%d') AS date, COUNT(*) AS value
              FROM mdc_msg_task
             WHERE status = 2
               AND send_time IS NOT NULL
               AND send_time >= #{startTime, jdbcType=TIMESTAMP}
             GROUP BY DATE_FORMAT(send_time, '%Y-%m-%d')
             ORDER BY date ASC
            """
    })
    List<Map<String, Object>> countByDay(@Param("startTime") LocalDateTime startTime);

    /**
     * 按日期范围统计消息发送趋势（执行成功），返回每日各类型数量和总量。
     *
     * @param startTime 开始时间（含）
     * @param endTime   结束时间（含）
     * @return key=date、noticeCount、smsCount、mailCount、totalCount
     */
    @Select({
            """
            SELECT DATE_FORMAT(send_time, '%Y-%m-%d') AS date,
                   SUM(CASE WHEN type = 1 THEN 1 ELSE 0 END) AS noticeCount,
                   SUM(CASE WHEN type = 2 THEN 1 ELSE 0 END) AS smsCount,
                   SUM(CASE WHEN type = 3 THEN 1 ELSE 0 END) AS mailCount,
                   COUNT(*) AS totalCount
              FROM mdc_msg_task
             WHERE status = 2
               AND send_time IS NOT NULL
               AND send_time >= #{startTime, jdbcType=TIMESTAMP}
               AND send_time < #{endTime, jdbcType=TIMESTAMP}
             GROUP BY DATE_FORMAT(send_time, '%Y-%m-%d')
             ORDER BY date ASC
            """
    })
    List<Map<String, Object>> countTrendByDayRange(@Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 本地查询消息分类分布（mdc_msg_task 表，条件 type=1 AND status=2）。
     */
    @Select({
            """
            SELECT msg_category AS code, COUNT(*) AS count
              FROM mdc_msg_task
             WHERE type = 1 AND status = 2 AND msg_category IS NOT NULL
             GROUP BY msg_category
             ORDER BY count DESC
            """
    })
    List<Map<String, Object>> countByCategoryLocal();

    /**
     * 按模板统计使用次数排行（mdc_msg_task 与 mdc_msg_template 关联）。
     */
    @Select({
            """
            SELECT t.id AS templateId, t.name AS name, COUNT(m.id) AS value
              FROM mdc_msg_template t
              LEFT JOIN mdc_msg_task m ON m.template_id = t.id
             WHERE t.id IS NOT NULL AND t.state = 1
             GROUP BY t.id, t.name
             ORDER BY value DESC
             LIMIT #{limit}
            """
    })
    List<Map<String, Object>> templateRank(@Param("limit") int limit);
}
