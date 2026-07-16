package top.mddata.open.mapper.admin;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.open.entity.admin.AppApply;
import top.mddata.open.entity.admin.base.AppApplyBase;

/**
 * 应用申请 映射层。
 *
 * @author henhen6
 * @since 2025-11-27 03:31:55
 */
@Repository
public interface AppApplyMapper extends SuperMapper<AppApply> {

    /**
     * 统计待审批申请数（audit_status=1）。
     */
    @Select({
            """
            SELECT COUNT(*) AS value
              FROM
            """
            + AppApplyBase.TABLE_NAME +
            """
             WHERE audit_status = 1
            """
    })
    Long countPending();
}