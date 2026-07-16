package top.mddata.open.mapper.admin;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.open.entity.admin.OauthLog;
import top.mddata.open.entity.admin.base.OauthLogBase;

import java.util.List;
import java.util.Map;

/**
 * 授权记录 映射层。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Repository
public interface OauthLogMapper extends SuperMapper<OauthLog> {

    /**
     * 按授权类型（grant_type）分组统计授权次数。
     */
    @Select({
            """
            SELECT grant_type AS name, COUNT(*) AS count
              FROM
            """
            + OauthLogBase.TABLE_NAME +
            """
             WHERE grant_type IS NOT NULL AND grant_type <> ''
             GROUP BY grant_type
             ORDER BY count DESC
            """
    })
    List<Map<String, Object>> countByGrantType();
}