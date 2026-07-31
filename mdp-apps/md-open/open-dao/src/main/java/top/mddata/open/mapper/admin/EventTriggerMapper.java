package top.mddata.open.mapper.admin;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.open.entity.admin.EventTrigger;
import top.mddata.open.entity.admin.base.EventTriggerBase;
import top.mddata.open.entity.admin.base.EventTypeBase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 事件触发 映射层。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
@Repository
public interface EventTriggerMapper extends SuperMapper<EventTrigger> {

    /**
     * 按事件编码分组统计触发次数（仅查询启用状态的事件类型）。
     *
     * <p>mdo_event_trigger / mdo_event_type 表均没有 deleted_at 字段。</p>
     *
     * @return key=eventCode、eventName、triggerCount
     */
    @Select({
            """
                    SELECT t.event_code AS eventCode,
                           COALESCE(ty.name, t.event_code) AS eventName,
                           COUNT(*) AS triggerCount
                      FROM
                    """
            + EventTriggerBase.TABLE_NAME + " t " +
            """
                     LEFT JOIN
                    """
            + EventTypeBase.TABLE_NAME + " ty ON ty.code = t.event_code AND ty.state = 1" +
            """
                     GROUP BY t.event_code, ty.name
                     ORDER BY triggerCount DESC
                    """
    })
    List<Map<String, Object>> countByEventCode();

    /**
     * 按事件编码分组统计触发次数排行 TOP N（仅查询启用状态的事件类型）。
     */
    @Select({
            """
                    SELECT t.event_code AS eventCode,
                           COALESCE(ty.name, t.event_code) AS eventName,
                           COUNT(*) AS triggerCount
                      FROM
                    """
            + EventTriggerBase.TABLE_NAME + " t " +
            """
                     LEFT JOIN
                    """
            + EventTypeBase.TABLE_NAME + " ty ON ty.code = t.event_code AND ty.state = 1" +
            """
                     GROUP BY t.event_code, ty.name
                     ORDER BY triggerCount DESC
                     LIMIT #{limit}
                    """
    })
    List<Map<String, Object>> rankByEventCode(@Param("limit") int limit);

    /**
     * 按天统计事件触发次数（指定日期范围）。
     */
    @Select({
            """
                    SELECT DATE_FORMAT(trigger_at, '%Y-%m-%d') AS date, COUNT(*) AS triggerCount
                      FROM
                    """
            + EventTriggerBase.TABLE_NAME +
            """
                     WHERE trigger_at >= #{startTime, jdbcType=TIMESTAMP}
                       AND trigger_at < #{endTime, jdbcType=TIMESTAMP}
                     GROUP BY DATE_FORMAT(trigger_at, '%Y-%m-%d')
                     ORDER BY date ASC
                    """
    })
    List<Map<String, Object>> countByDayRange(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);
}