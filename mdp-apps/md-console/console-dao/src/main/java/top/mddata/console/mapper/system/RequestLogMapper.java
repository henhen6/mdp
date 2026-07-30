package top.mddata.console.mapper.system;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.console.entity.system.RequestLog;
import top.mddata.console.entity.system.base.RequestLogBase;

import java.util.List;
import java.util.Map;

/**
 * 请求日志 映射层。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Repository
public interface RequestLogMapper extends SuperMapper<RequestLog> {

    /**
     * 统计异常请求数（abnormal=1）。
     */
    @Select({
            """
                    SELECT COUNT(*) AS value
                      FROM
                    """
            + RequestLogBase.TABLE_NAME +
            """
                     WHERE abnormal = 1
                    """
    })
    Long countAbnormal();

    /**
     * 按日志类型分组统计请求次数（1-查询 2-新增 3-修改 4-删除 9-其他）。
     *
     * @return key=name、count
     */
    @Select({
            """
                    SELECT log_type AS code, COUNT(*) AS count
                      FROM
                    """
            + RequestLogBase.TABLE_NAME +
            """
                     GROUP BY log_type
                     ORDER BY count DESC
                    """
    })
    List<Map<String, Object>> countByLogType();

    /**
     * 按省份统计请求次数（用于地图展示）。
     */
    @Select({
            """
                    SELECT ip_province AS province, COUNT(*) AS count
                      FROM
                    """
            + RequestLogBase.TABLE_NAME +
            """
                     WHERE ip_province IS NOT NULL AND ip_province <> ''
                     GROUP BY ip_province
                     ORDER BY count DESC
                     LIMIT 50
                    """
    })
    List<Map<String, Object>> countByProvince();

    /**
     * 按耗时区间（毫秒）分组统计请求次数。
     *
     * <p>区间：&lt;100ms / 100-500ms / 500ms-1s / 1s-3s / &gt;=3s</p>
     */
    @Select({
            """
                    SELECT CASE
                             WHEN consuming_time < 100 THEN '<100ms'
                             WHEN consuming_time < 500 THEN '100-500ms'
                             WHEN consuming_time < 1000 THEN '500ms-1s'
                             WHEN consuming_time < 3000 THEN '1s-3s'
                             ELSE '>=3s'
                           END AS name,
                           COUNT(*) AS count
                      FROM
                    """
            + RequestLogBase.TABLE_NAME +
            """
                     WHERE consuming_time IS NOT NULL
                     GROUP BY CASE
                             WHEN consuming_time < 100 THEN '<100ms'
                             WHEN consuming_time < 500 THEN '100-500ms'
                             WHEN consuming_time < 1000 THEN '500ms-1s'
                             WHEN consuming_time < 3000 THEN '1s-3s'
                             ELSE '>=3s'
                           END
                     ORDER BY MIN(consuming_time)
                    """
    })
    List<Map<String, Object>> countByConsumingRange();

    /**
     * 统计总请求量。
     */
    @Select({
            """
                    SELECT COUNT(*) AS value
                      FROM
                    """
            + RequestLogBase.TABLE_NAME
    })
    Long countTotal();

    /**
     * 统计成功请求数量（abnormal=0）。
     */
    @Select({
            """
                    SELECT COUNT(*) AS value
                      FROM
                    """
            + RequestLogBase.TABLE_NAME +
            """
                     WHERE abnormal = 0
                    """
    })
    Long countSuccess();

    /**
     * 按IP地址统计请求次数排行。
     */
    @Select({
            """
                    SELECT ip_address AS ipAddress, COUNT(*) AS count
                      FROM
                    """
            + RequestLogBase.TABLE_NAME +
            """
                     WHERE ip_address IS NOT NULL AND ip_address <> ''
                     GROUP BY ip_address
                     ORDER BY count DESC
                     LIMIT #{limit}
                    """
    })
    List<Map<String, Object>> countByIpRank(int limit);

    /**
     * 按请求接口（class_path + method_name）统计请求次数排行。
     * <p>返回字段：classPath、methodName、httpUri、httpMethod、description、count</p>
     */
    @Select({
            """
                    SELECT class_path AS classPath,
                           method_name AS methodName,
                           http_uri AS httpUri,
                           http_method AS httpMethod,
                           description,
                           COUNT(*) AS count
                      FROM
                    """
            + RequestLogBase.TABLE_NAME +
            """
                     WHERE class_path IS NOT NULL AND method_name IS NOT NULL
                     GROUP BY class_path, method_name, http_uri, http_method, description
                     ORDER BY count DESC
                     LIMIT #{limit}
                    """
    })
    List<Map<String, Object>> countByInterfaceRank(int limit);
}