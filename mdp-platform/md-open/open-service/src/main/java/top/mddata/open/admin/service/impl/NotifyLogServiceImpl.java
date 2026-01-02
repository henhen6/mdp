package top.mddata.open.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.entity.NotifyLog;
import top.mddata.open.admin.mapper.NotifyLogMapper;
import top.mddata.open.admin.service.NotifyLogService;

/**
 * 回调日志 服务层实现。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyLogServiceImpl extends SuperServiceImpl<NotifyLogMapper, NotifyLog> implements NotifyLogService {

}
