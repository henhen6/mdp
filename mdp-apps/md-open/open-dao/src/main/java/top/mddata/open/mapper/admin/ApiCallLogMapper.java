package top.mddata.open.mapper.admin;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.open.entity.admin.ApiCallLog;
import top.mddata.open.entity.admin.base.ApiCallLogBase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 调用日志 映射层。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@Repository
public interface ApiCallLogMapper extends SuperMapper<ApiCallLog> {


    /**
     * 按日统计调用总量与失败量（指定日期范围）。
     *
     * @return 每日统计，key=date、callCount、failCount
     */
    @Select({
            """
                    SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS date,
                           COUNT(*) AS callCount,
                           SUM(CASE WHEN exec_status = '2' THEN 1 ELSE 0 END) AS failCount
                      FROM
                    """
            + ApiCallLogBase.TABLE_NAME +
            """
                     WHERE created_at >= #{startTime, jdbcType=TIMESTAMP}
                       AND created_at < #{endTime, jdbcType=TIMESTAMP}
                     GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d')
                     ORDER BY date ASC
                    """
    })
    List<Map<String, Object>> countByDayRange(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 按应用统计调用次数排行 TOP N。
     *
     * @return key=appId、appName、callCount
     */
    @Select({
            """
                    SELECT app_id AS appId,
                           MAX(app_name) AS appName,
                           COUNT(*) AS callCount
                      FROM
                    """
            + ApiCallLogBase.TABLE_NAME +
            """
                     GROUP BY app_id
                     ORDER BY callCount DESC
                     LIMIT #{limit}
                    """
    })
    List<Map<String, Object>> rankByApp(@Param("limit") int limit);

    /**
     * 按 API 统计调用次数排行 TOP N。
     *
     * @return key=apiId、apiName、appName、callCount
     */
    @Select({
            """
                    SELECT api_id AS apiId,
                           MAX(api_name) AS apiName,
                           MAX(app_name) AS appName,
                           COUNT(*) AS callCount
                      FROM
                    """
            + ApiCallLogBase.TABLE_NAME +
            """
                     GROUP BY api_id
                     ORDER BY callCount DESC
                     LIMIT #{limit}
                    """
    })
    List<Map<String, Object>> rankByApi(@Param("limit") int limit);

    /**
     * 汇总今日成功次数与总次数（created_at 在指定时间之后）。
     * execStatus: 1-执行成功 2-执行失败
     *
     * @return key=successCount、totalCount
     */
    @Select({
            """
                    SELECT COALESCE(SUM(CASE WHEN exec_status = '1' THEN 1 ELSE 0 END), 0) AS successCount,
                           COALESCE(SUM(CASE WHEN exec_status IN ('1', '2') THEN 1 ELSE 0 END), 0) AS totalCount
                      FROM
                    """
            + ApiCallLogBase.TABLE_NAME
    })
    Map<String, Object> sumAll();
}