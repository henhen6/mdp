package top.mddata.open.mapper.admin;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.open.entity.admin.App;
import top.mddata.open.entity.admin.base.AppBase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 应用 映射层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Repository
public interface AppMapper extends SuperMapper<App> {

    /**
     * 按日统计新增应用数（指定起始时间之后）。
     *
     * <p>mdo_app 表没有 deleted_at 字段。</p>
     *
     * @param startTime 起始时间（包含）
     * @return 每日新增应用数，key=date(yyyy-MM-dd)、value=count
     */
    @Select({
            """
                    SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS date, COUNT(*) AS value
                      FROM
                    """
            + AppBase.TABLE_NAME +
            """
                     WHERE created_at >= #{startTime, jdbcType=TIMESTAMP}
                     GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d')
                     ORDER BY date ASC
                    """
    })
    List<Map<String, Object>> countByDay(@Param("startTime") LocalDateTime startTime);
}