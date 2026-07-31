package top.mddata.open.facade.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.dto.admin.EventTriggerDto;
import top.mddata.open.dto.admin.NotifyInfoDto;
import top.mddata.open.entity.admin.EventTrigger;
import top.mddata.open.facade.admin.NotifyAndEventPushFacade;
import top.mddata.open.service.admin.EventTriggerService;
import top.mddata.open.service.admin.NotifyInfoService;

/**
 *
 * @author henhen
 * @since 2026/1/13 00:09
 */
@Service
@RequiredArgsConstructor
public class NotifyAndEventPushFacadeImpl implements NotifyAndEventPushFacade {
    private final NotifyInfoService notifyInfoService;
    private final EventTriggerService eventTriggerService;

    @Override
    public R<Long> apiCallNotify(NotifyInfoDto request) {
        return notifyInfoService.notify(request);
    }

    @Override
    public R<EventTrigger> eventPush(EventTriggerDto request) {
        return R.success(eventTriggerService.save(request));
    }
}
