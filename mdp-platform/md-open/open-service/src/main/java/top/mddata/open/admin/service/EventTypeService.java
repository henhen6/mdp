package top.mddata.open.admin.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.admin.entity.EventType;

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
}
