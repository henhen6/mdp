package top.mddata.console.mapper.message;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.console.entity.message.InterfaceLog;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 接口执行日志记录 映射层。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@Repository
public interface InterfaceLogMapper extends SuperMapper<InterfaceLog> {

    /**
     * 汇总今日成功次数、失败次数与总次数（exec_start_time 在指定时间之后，按 status 分组）。
     * status: 1-初始化 2-成功 3-失败
     *
     * @return key=successCount、failCount、totalCount
     */
    @Select({
            """
                    SELECT COALESCE(SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END), 0) AS successCount,
                           COALESCE(SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END), 0) AS failCount,
                           COALESCE(SUM(CASE WHEN status IN (2, 3) THEN 1 ELSE 0 END), 0) AS totalCount
                      FROM
                    """
            + InterfaceLog.TABLE_NAME +
            """
                     WHERE exec_start_time >= #{startTime, jdbcType=TIMESTAMP}
                    """
    })
    Map<String, Object> sumToday(@Param("startTime") LocalDateTime startTime);

    /**
     * 汇总全量成功次数、失败次数与总次数（按 status 分组）。
     * status: 1-初始化 2-成功 3-失败
     *
     * @return key=successCount、failCount、totalCount
     */
    @Select({
            """
                    SELECT COALESCE(SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END), 0) AS successCount,
                           COALESCE(SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END), 0) AS failCount,
                           COALESCE(SUM(CASE WHEN status IN (2, 3) THEN 1 ELSE 0 END), 0) AS totalCount
                      FROM
                    """
            + InterfaceLog.TABLE_NAME
    })
    Map<String, Object> sumAll();
}
