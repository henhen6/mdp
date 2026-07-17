package top.mddata.open.mapper.admin;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.open.entity.admin.EventPush;
import top.mddata.open.entity.admin.base.EventPushBase;
import top.mddata.open.entity.admin.base.EventTypeBase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 事件推送任务 映射层。
 *
 * @author henhen6
 * @since 2026-01-12 21:28:36
 */
@Repository
public interface EventPushMapper extends SuperMapper<EventPush> {

    /**
     * 按事件编码+应用分组统计推送次数（关联 event_type 取事件名称，关联 mdo_app 取应用名称）。
     *
     * <p>推送次数 = event_push 表中按 event_code+app_id 分组的记录数。
     * 由于一个事件触发可能推送给多个应用，所以此处的"推送"以 event_push 行数计。</p>
     *
     * @return key=eventCode、eventName、appId、appName、pushCount
     */
    @Select({
            """
            SELECT p.event_code AS eventCode,
                   COALESCE(ty.name, p.event_code) AS eventName,
                   p.app_id AS appId,
                   COALESCE(a.name, p.app_id) AS appName,
                   COUNT(*) AS pushCount
              FROM
            """
            + EventPushBase.TABLE_NAME + " p" +
            """
             LEFT JOIN
            """
            + EventTypeBase.TABLE_NAME + " ty ON ty.code = p.event_code AND ty.state = 1" +
            """
             LEFT JOIN mdo_app a ON a.id = p.app_id
             GROUP BY p.event_code, ty.name, p.app_id, a.name
             ORDER BY pushCount DESC
            """
    })
    List<Map<String, Object>> statisticsByEventAndApp();

    /**
     * 按天统计触发次数与推送次数（关联 event_trigger 的 trigger_at）。
     *
     * <p>触发次数 = event_trigger 唯一记录数；推送次数 = event_push 唯一记录数。
     * 仅统计 event_trigger.trigger_at 在指定时间之后的记录。</p>
     */
    @Select({
            """
            SELECT DATE_FORMAT(t.trigger_at, '%Y-%m-%d') AS date,
                   COUNT(DISTINCT t.id) AS triggerCount,
                   COUNT(DISTINCT p.id) AS pushCount
              FROM
            """
            + EventPushBase.TABLE_NAME + " p" +
            """
             INNER JOIN mdo_event_trigger t ON t.id = p.event_trigger_id
             WHERE t.trigger_at >= #{startTime, jdbcType=TIMESTAMP}
               AND t.trigger_at < #{endTime, jdbcType=TIMESTAMP}
             GROUP BY DATE_FORMAT(t.trigger_at, '%Y-%m-%d')
             ORDER BY date ASC
            """
    })
    List<Map<String, Object>> countByDayRange(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
}