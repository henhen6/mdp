package top.mddata.open.service.notify.event.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.entity.admin.App;
import top.mddata.open.entity.admin.AppKeys;
import top.mddata.open.entity.admin.EventSubscription;
import top.mddata.open.entity.admin.EventType;
import top.mddata.open.mapper.admin.EventSubscriptionMapper;
import top.mddata.open.service.notify.event.EventSubscriptionService;
import top.mddata.open.vo.admin.AppKeysVo;

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

    @Override
    @Transactional(readOnly = true)
    public List<AppKeysVo> findByEventCode(String eventCode) {
        Iterable<QueryColumn> queryColumns = QueryMethods.allColumns(AppKeys.class);
        QueryColumn appId = QueryMethods.column(App::getId).as("app_id");
        QueryColumn appName = QueryMethods.column(App::getName).as("app_name");
        QueryWrapper wrapper = QueryWrapper.create()
                .select(queryColumns)
                .select(App::getAppKey, App::getAppSecret)
                .select(appId, appName)
                .from(AppKeys.class)
                .innerJoin(App.class).on(App::getId, AppKeys::getAppId)
                .innerJoin(EventSubscription.class).on(EventSubscription::getAppId, App::getId)
                .innerJoin(EventType.class).on(EventType::getId, EventSubscription::getEventTypeId)
                .where(EventType::getCode).eq(eventCode)
                .eq(AppKeys::getNotifyState, true)
                .eq(App::getState, true);

        return listAs(wrapper, AppKeysVo.class);
    }

}
