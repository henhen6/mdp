package top.mddata.open.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.entity.EventSubscription;
import top.mddata.open.admin.mapper.EventSubscriptionMapper;
import top.mddata.open.admin.service.EventSubscriptionService;

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

}
