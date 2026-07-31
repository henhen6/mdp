package top.mddata.open.service.admin.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.entity.admin.NotifyInfoLog;
import top.mddata.open.mapper.admin.NotifyInfoLogMapper;
import top.mddata.open.service.admin.NotifyInfoLogService;

/**
 * 回调任务日志 服务层实现。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyInfoLogServiceImpl extends SuperServiceImpl<NotifyInfoLogMapper, NotifyInfoLog> implements NotifyInfoLogService {

}
