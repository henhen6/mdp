package top.mddata.open.service.echo;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.interfaces.echo.LoadService;
import top.mddata.base.utils.CollHelper;
import top.mddata.open.entity.admin.EventPush;
import top.mddata.open.service.notify.event.EventPushService;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 事件推送回显：按主键批量查询，拼接「应用：{appKey} 事件：{eventCode}」。
 * 数据属 open 模块自有表，直接走本地 Service，boot/cloud 两种部署均生效。
 *
 * @author henhen6
 */
@Service
@RequiredArgsConstructor
public class EventPushEchoImpl implements LoadService {
    private final EventPushService eventPushService;

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<EventPush> list = eventPushService.listByIds(ids);
        return CollHelper.uniqueIndex(list, EventPush::getId,
                item -> "应用：" + item.getAppKey() + " 事件：" + item.getEventCode());
    }
}
