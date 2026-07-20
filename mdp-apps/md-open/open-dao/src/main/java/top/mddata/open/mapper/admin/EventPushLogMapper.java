package top.mddata.open.mapper.admin;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.open.entity.admin.EventPushLog;
import top.mddata.open.entity.admin.base.EventPushLogBase;

import java.util.Map;

/**
 * 事件推送日志 映射层。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
@Repository
public interface EventPushLogMapper extends SuperMapper<EventPushLog> {

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
            + EventPushLogBase.TABLE_NAME
    })
    Map<String, Object> sumAll();
}
