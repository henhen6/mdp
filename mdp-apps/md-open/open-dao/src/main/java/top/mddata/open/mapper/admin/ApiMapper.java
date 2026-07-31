package top.mddata.open.mapper.admin;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.open.entity.admin.Api;
import top.mddata.open.entity.admin.base.ApiBase;

/**
 * 开放接口 映射层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Repository
public interface ApiMapper extends SuperMapper<Api> {

    /**
     * 统计启用状态 API 总数（state=1）。
     */
    @Select({
            """
                    SELECT COUNT(*) AS value
                      FROM
                    """
            + ApiBase.TABLE_NAME +
            """
                     WHERE state = 1
                    """
    })
    Long countEnabled();
}