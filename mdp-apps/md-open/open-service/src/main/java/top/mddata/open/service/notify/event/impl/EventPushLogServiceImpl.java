package top.mddata.open.service.notify.event.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.entity.admin.EventPushLog;
import top.mddata.open.mapper.admin.EventPushLogMapper;
import top.mddata.open.service.notify.event.EventPushLogService;

/**
 * 事件推送日志 服务层实现。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventPushLogServiceImpl extends SuperServiceImpl<EventPushLogMapper, EventPushLog> implements EventPushLogService {

}
