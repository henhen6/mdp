package top.mddata.open.service.notify.event;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.entity.admin.EventType;

/**
 * 事件类型 服务层。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
public interface EventTypeService extends SuperService<EventType> {

    /**
     * 校验事件类型编码是否重复。
     *
     * @param code 事件类型编码
     * @param id   事件类型ID
     * @return true:重复 false:不重复
     */
    Boolean check(String code, Long id);

    /**
     * 根据事件类型编码获取事件类型。
     *
     * @param eventCode 事件类型编码
     * @return 事件类型
     */
    EventType getByCode(String eventCode);
}
