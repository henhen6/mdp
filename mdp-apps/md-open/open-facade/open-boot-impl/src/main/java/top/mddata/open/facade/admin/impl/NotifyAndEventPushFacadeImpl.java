package top.mddata.open.facade.admin.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.open.dto.admin.EventTriggerDto;
import top.mddata.open.dto.admin.NotifyInfoDto;
import top.mddata.open.entity.admin.EventTrigger;
import top.mddata.open.facade.admin.NotifyAndEventPushFacade;
import top.mddata.open.service.notify.event.EventTriggerService;
import top.mddata.open.service.notify.callback.NotifyInfoService;

/**
 *
 * @author henhen
 * @since 2026/1/13 00:09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyAndEventPushFacadeImpl implements NotifyAndEventPushFacade {
    private final NotifyInfoService notifyInfoService;
    private final EventTriggerService eventTriggerService;

    /**
     * 单体路径边界：回调任务创建失败不能影响 md-api 主业务流程，
     * Service 层保持 fail fast 抛异常，这里 catch-all 降级为 R.fail。
     */
    @Override
    public R<Long> apiCallNotify(NotifyInfoDto request) {
        try {
            return notifyInfoService.notify(request);
        } catch (Exception e) {
            log.error("回调任务创建失败：{}", e.getMessage(), e);
            return R.fail("回调任务创建失败：" + e.getMessage());
        }
    }

    @Override
    public R<EventTrigger> eventPush(EventTriggerDto request) {
        return R.success(eventTriggerService.save(request));
    }
}
