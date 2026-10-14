package top.mddata.open.service.notify.event;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.dto.admin.EventTriggerDto;
import top.mddata.open.entity.admin.EventTrigger;

/**
 * 事件触发 服务层。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
public interface EventTriggerService extends SuperService<EventTrigger> {
    /**
     * 保存事件触发
     *
     * @param save 事件触发参数
     * @return 事件触发
     */
    EventTrigger save(EventTriggerDto save);
}
