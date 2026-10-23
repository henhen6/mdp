package top.mddata.open.service.notify.event;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.entity.admin.EventPush;

import java.time.LocalDateTime;

/**
 * 事件推送任务 服务层接口。
 */
public interface EventPushService extends SuperService<EventPush> {

    /** 按事件触发生成订阅方推送任务，事务提交后批量异步投递 */
    void saveByEventTrigger(String eventCode, Long triggerId, String requestData);

    /** 手动结束重试（仅 FAIL 状态可结束） */
    Boolean end(Long id);

    /** 手动立即推送：状态重置后异步投递 */
    Long executeImmediately(Long id);

    /** 重试扫描（PowerJob 定时调用） */
    void retry(LocalDateTime now, int limit);
}
