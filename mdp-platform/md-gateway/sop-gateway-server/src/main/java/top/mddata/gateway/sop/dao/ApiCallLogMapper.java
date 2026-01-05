package top.mddata.gateway.sop.dao;

import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.gateway.sop.pojo.entity.ApiCallLog;

/**
 * 调用日志 映射层。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@Repository
public interface ApiCallLogMapper extends SuperMapper<ApiCallLog> {

}
