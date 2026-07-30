package top.mddata.console.mapper.system;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.console.entity.system.File;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件 映射层。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
@Repository
public interface FileMapper extends SuperMapper<File> {

    /**
     * 统计文件总容量（字节）
     *
     * @return 文件总容量
     */
    @Select("SELECT COALESCE(SUM(file_size), 0) FROM mdc_file")
    Long sumFileSize();

    /**
     * 统计指定时间之后的文件总数与文件总容量。
     *
     * <p>mdc_file 表无逻辑删除字段。</p>
     *
     * @param startTime 起始时间（包含）
     * @return key=fileCount、totalSize
     */
    @Select({
            """
                    SELECT COUNT(*) AS fileCount, COALESCE(SUM(file_size), 0) AS totalSize
                      FROM mdc_file
                     WHERE created_at >= #{startTime, jdbcType=TIMESTAMP}
                    """
    })
    Map<String, Object> statAfter(@Param("startTime") LocalDateTime startTime);

    /**
     * 按文件类型统计。
     */
    @Select({
            """
                    SELECT file_type AS code, COUNT(*) AS count
                      FROM mdc_file
                     WHERE file_type IS NOT NULL
                     GROUP BY file_type
                     ORDER BY count DESC
                    """
    })
    List<Map<String, Object>> countByFileType();

    /**
     * 按业务类型统计。
     */
    @Select({
            """
                    SELECT object_type AS name, COUNT(*) AS count
                      FROM mdc_file
                     WHERE object_type IS NOT NULL
                       AND object_type <> ''
                     GROUP BY object_type
                     ORDER BY count DESC
                    """
    })
    List<Map<String, Object>> countByObjectType();

    /**
     * 按存储平台统计。
     */
    @Select({
            """
                    SELECT platform AS name, COUNT(*) AS count
                      FROM mdc_file
                     WHERE platform IS NOT NULL
                       AND platform <> ''
                     GROUP BY platform
                     ORDER BY count DESC
                    """
    })
    List<Map<String, Object>> countByPlatform();

    /**
     * 按文件大小区间统计。
     *
     * <p>区间：&lt;1MB / 1-10MB / 10-100MB / 100MB-1GB / &gt;=1GB</p>
     */
    @Select({
            """
                    SELECT bucket AS name, COUNT(*) AS count
                      FROM (
                          SELECT CASE
                              WHEN file_size < 1048576 THEN '<1MB'
                              WHEN file_size < 10485760 THEN '1-10MB'
                              WHEN file_size < 104857600 THEN '10-100MB'
                              WHEN file_size < 1073741824 THEN '100MB-1GB'
                              ELSE '>=1GB'
                          END AS bucket
                          FROM mdc_file
                          WHERE file_size IS NOT NULL
                      ) t
                     GROUP BY bucket
                     ORDER BY MIN(CASE bucket
                                    WHEN '<1MB' THEN 1
                                    WHEN '1-10MB' THEN 2
                                    WHEN '10-100MB' THEN 3
                                    WHEN '100MB-1GB' THEN 4
                                    ELSE 5
                                 END)
                    """
    })
    List<Map<String, Object>> countBySizeRange();

    /**
     * 按日统计新增文件数与新增容量。
     */
    @Select({
            """
                    SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS date,
                           COUNT(*) AS fileCount,
                           COALESCE(SUM(file_size), 0) AS totalSize
                      FROM mdc_file
                     WHERE created_at >= #{startTime, jdbcType=TIMESTAMP}
                       AND created_at < #{endTime, jdbcType=TIMESTAMP}
                     GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d')
                     ORDER BY date ASC
                    """
    })
    List<Map<String, Object>> countByDayRange(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计临时文件数量和容量（object_type = 'temp'）。
     *
     * @return key=fileCount、totalSize
     */
    @Select({
            """
                    SELECT COUNT(*) AS fileCount, COALESCE(SUM(file_size), 0) AS totalSize
                      FROM mdc_file
                     WHERE object_type = #{objectType}
                    """
    })
    Map<String, Object> statByObjectType(@Param("objectType") String objectType);
}