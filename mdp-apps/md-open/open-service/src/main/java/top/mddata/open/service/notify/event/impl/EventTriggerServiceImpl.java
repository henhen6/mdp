package top.mddata.open.service.notify.event.impl;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.open.dto.admin.EventTriggerDto;
import top.mddata.open.entity.admin.EventTrigger;
import top.mddata.open.entity.admin.EventType;
import top.mddata.open.mapper.admin.EventTriggerMapper;
import top.mddata.open.service.notify.event.EventPushService;
import top.mddata.open.service.notify.event.EventTriggerService;
import top.mddata.open.service.notify.event.EventTypeService;

import java.time.LocalDateTime;

/**
 * 事件触发 服务层实现。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventTriggerServiceImpl extends SuperServiceImpl<EventTriggerMapper, EventTrigger> implements EventTriggerService {
    private final EventTypeService eventTypeService;
    private final EventPushService eventPushService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventTrigger save(EventTriggerDto save) {
        EventTrigger entity = BeanUtil.toBean(save, EventTrigger.class);
        entity.setId(null);
        EventType eventType = eventTypeService.getByCode(entity.getEventCode());
        ArgumentAssert.notNull(eventType, "事件编码不存在");
        entity.setEventId(eventType.getId());
        if (entity.getTriggerAt() == null) {
            entity.setTriggerAt(LocalDateTime.now());
        }

        save(entity);
        log.info("[事件触发] 事件日志: {}---{} 保存成功", save.getEventCode(), save.getEventContent());
        eventPushService.saveByEventTrigger(eventType.getCode(), entity.getId(), entity.getEventContent());
        return entity;
    }

}
