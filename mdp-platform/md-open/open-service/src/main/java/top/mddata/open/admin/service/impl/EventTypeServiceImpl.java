package top.mddata.open.admin.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.open.admin.dto.EventTypeDto;
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
    @Override
    public Boolean check(String code, Long id) {
        ArgumentAssert.notEmpty(code, "请填写事件类型标识");
        return count(QueryWrapper.create().eq(EventType::getCode, code).ne(EventType::getId, id)) > 0;
    }

    @Override
    protected EventType saveBefore(Object save) {
        EventTypeDto dto = (EventTypeDto) save;
        ArgumentAssert.isFalse(check(dto.getCode(), null), "事件类型标识已存在");
        return super.saveBefore(save);
    }

    @Override
    protected EventType updateBefore(Object update) {
        EventTypeDto dto = (EventTypeDto) update;
        ArgumentAssert.isFalse(check(dto.getCode(), dto.getId()), "事件类型标识已存在");
        return super.updateBefore(update);
    }
}
