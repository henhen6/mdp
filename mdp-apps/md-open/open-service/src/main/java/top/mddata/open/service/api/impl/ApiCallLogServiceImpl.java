package top.mddata.open.service.api.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.entity.admin.ApiCallLog;
import top.mddata.open.mapper.admin.ApiCallLogMapper;
import top.mddata.open.service.api.ApiCallLogService;

/**
 * 调用日志 服务层实现。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ApiCallLogServiceImpl extends SuperServiceImpl<ApiCallLogMapper, ApiCallLog> implements ApiCallLogService {

}
