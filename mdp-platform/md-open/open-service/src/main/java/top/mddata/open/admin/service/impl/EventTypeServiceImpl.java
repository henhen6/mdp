package top.mddata.open.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.entity.EventType;
import top.mddata.open.admin.mapper.EventTypeMapper;
import top.mddata.open.admin.service.EventTypeService;

/**
 * 事件类型 服务层实现。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventTypeServiceImpl extends SuperServiceImpl<EventTypeMapper, EventType> implements EventTypeService {

}
