package top.mddata.console.message.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.console.message.entity.MsgTask;
import top.mddata.console.message.mapper.MsgTaskMapper;
import top.mddata.console.message.service.MsgTaskService;

/**
 * 消息任务 服务层实现。
 *
 * @author henhen6
 * @since 2025-12-21 00:02:22
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MsgTaskServiceImpl extends SuperServiceImpl<MsgTaskMapper, MsgTask> implements MsgTaskService {

}
