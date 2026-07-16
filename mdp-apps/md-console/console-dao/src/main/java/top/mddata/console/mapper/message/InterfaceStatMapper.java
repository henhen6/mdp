package top.mddata.console.mapper.message;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.console.entity.message.InterfaceStat;
import top.mddata.console.entity.message.base.InterfaceStatBase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 接口统计 映射层。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@Repository
public interface InterfaceStatMapper extends SuperMapper<InterfaceStat> {

    /**
     * 汇总今日成功次数、失败次数与总次数（last_exec_at 在指定时间之后）。
     *
     * @return key=successCount、failCount、totalCount
     */
    @Select({
            """
            SELECT COALESCE(SUM(success_count), 0) AS successCount,
                   COALESCE(SUM(fail_count), 0) AS failCount,
                   COALESCE(SUM(success_count + fail_count), 0) AS totalCount
              FROM
            """
            + InterfaceStatBase.TABLE_NAME +
            """
             WHERE last_exec_at >= #{startTime, jdbcType=TIMESTAMP}
            """
    })
    Map<String, Object> sumAfter(@Param("startTime") LocalDateTime startTime);

    /**
     * 按接口调用总次数排行 TOP N。
     */
    @Select({
            """
            SELECT id, name,
                   success_count AS successCount,
                   fail_count AS failCount,
                   (success_count + fail_count) AS totalCount
              FROM
            """
            + InterfaceStatBase.TABLE_NAME +
            """
             ORDER BY (success_count + fail_count) DESC
             LIMIT #{limit}
            """
    })
    List<Map<String, Object>> rankByTotalCount(@Param("limit") int limit);

    /**
     * 按接口失败次数排行 TOP N。
     */
    @Select({
            """
            SELECT id, name,
                   success_count AS successCount,
                   fail_count AS failCount,
                   (success_count + fail_count) AS totalCount
              FROM
            """
            + InterfaceStatBase.TABLE_NAME +
            """
             ORDER BY fail_count DESC
             LIMIT #{limit}
            """
    })
    List<Map<String, Object>> rankByFailCount(@Param("limit") int limit);
}