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
            SELECT type AS code,
                   CASE type
                       WHEN 1 THEN '站内信'
                       WHEN 2 THEN '短信'
                       WHEN 3 THEN '邮件'
                       ELSE CONCAT('其他-', type)
                   END AS name,
                   COUNT(*) AS count
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
     * 按模板统计使用次数排行（mdc_msg_task 与 mdc_msg_template 关联）。
     */
    @Select({
            """
            SELECT t.id AS templateId, t.name AS name, COUNT(m.id) AS value
              FROM mdc_msg_template t
              LEFT JOIN mdc_msg_task m ON m.template_id = t.id
             WHERE t.id IS NOT NULL
             GROUP BY t.id, t.name
             ORDER BY value DESC
             LIMIT #{limit}
            """
    })
    List<Map<String, Object>> templateRank(@Param("limit") int limit);
}
