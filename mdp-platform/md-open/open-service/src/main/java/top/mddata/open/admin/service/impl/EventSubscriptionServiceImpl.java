package top.mddata.open.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.entity.EventSubscription;
import top.mddata.open.admin.mapper.EventSubscriptionMapper;
import top.mddata.open.admin.service.EventSubscriptionService;

import java.util.List;

/**
 * 事件订阅 服务层实现。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventSubscriptionServiceImpl extends SuperServiceImpl<EventSubscriptionMapper, EventSubscription> implements EventSubscriptionService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveEventSubscriptionByAppId(Long appId, List<Long> eventTypeIdList) {
        remove(QueryWrapper.create().eq(EventSubscription::getAppId, appId));

        if (CollUtil.isNotEmpty(eventTypeIdList)) {
            List<EventSubscription> list = eventTypeIdList.stream().map(eventTypeId -> {
                EventSubscription subscription = new EventSubscription();
                subscription.setAppId(appId).setEventTypeId(eventTypeId);
                return subscription;
            }).toList();
            saveBatch(list);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listEventTypeIdByAppId(Long appId) {
        List<EventSubscription> eventSubscriptionList = list(QueryWrapper.create().eq(EventSubscription::getAppId, appId));
        return eventSubscriptionList.stream().map(EventSubscription::getEventTypeId).toList();
    }
}
