package top.mddata.open.service.echo;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.interfaces.echo.LoadService;
import top.mddata.base.utils.CollHelper;
import top.mddata.open.entity.admin.ApiCallLog;
import top.mddata.open.service.api.ApiCallLogService;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 接口调用日志回显：按主键批量查询 traceId。
 * 数据属 open 模块自有表，直接走本地 Service，boot/cloud 两种部署均生效。
 *
 * @author henhen6
 */
@Service
@RequiredArgsConstructor
public class ApiCallLogEchoImpl implements LoadService {
    private final ApiCallLogService apiCallLogService;

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<ApiCallLog> list = apiCallLogService.listByIds(ids);
        return CollHelper.uniqueIndex(list, ApiCallLog::getId, ApiCallLog::getTraceId);
    }
}
