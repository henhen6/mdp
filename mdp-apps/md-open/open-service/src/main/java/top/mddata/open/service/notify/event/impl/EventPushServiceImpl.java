package top.mddata.open.service.notify.event.impl;

import com.gitee.sop.support.exception.OpenException;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.enumeration.StoryMessageEnum;
import top.mddata.open.entity.admin.EventPush;
import top.mddata.open.enumeration.admin.ExecStatusEnum;
import top.mddata.open.mapper.admin.EventPushMapper;
import top.mddata.open.service.notify.event.EventPushService;
import top.mddata.open.service.notify.event.EventSubscriptionService;
import top.mddata.open.service.notify.event.EventPushTaskExecutor;
import top.mddata.open.service.notify.pipeline.NotifyTaskDispatcher;
import top.mddata.open.service.notify.pipeline.NotifyUrlValidator;
import top.mddata.open.service.notify.pipeline.RetryPolicy;
import top.mddata.open.vo.admin.AppKeysVo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * 事件推送任务 服务层实现。
 * 投递/重试/日志管道已下沉到 notify.pipeline，本类只负责任务生成、状态查询与调度触发。
 *
 * @author henhen6
 * @since 2026-01-12 21:28:36
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EventPushServiceImpl extends SuperServiceImpl<EventPushMapper, EventPush> implements EventPushService {

    /** WAIT 状态超过该分钟数视为「投递丢失」，由重试扫描兜底 */
    private static final int WAIT_LOST_MINUTES = 5;

    private final EventSubscriptionService eventSubscriptionService;
    private final RetryPolicy retryPolicy;
    private final NotifyTaskDispatcher notifyTaskDispatcher;
    private final EventPushTaskExecutor eventPushTaskExecutor;
    private final NotifyUrlValidator notifyUrlValidator;

    /**
     * 按事件触发记录生成各订阅应用的推送任务：事务内只写库，提交后批量异步投递
     * （修旧版在事务内向线程池提交任务、长事务占连接的问题）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveByEventTrigger(String eventCode, Long triggerId, String requestData) {
        List<AppKeysVo> appKeysList = eventSubscriptionService.findByEventCode(eventCode);
        log.info("[事件触发] 事件类型：{}，订阅该事件的应用数量：{}", eventCode, appKeysList.size());
        if (appKeysList.isEmpty()) {
            return;
        }

        // 逐个校验订阅方回调地址，非法地址跳过该订阅方，不阻断其他订阅方、不回滚事务（SSRF 防护）
        List<AppKeysVo> validAppKeysList = appKeysList.stream().filter(appKeys -> {
            String notifyUrl = appKeys.getNotifyUrl();
            if (notifyUrl == null || notifyUrl.isBlank()) {
                log.warn("[事件触发] 订阅方回调地址为空，跳过：eventCode={}, appKey={}", eventCode, appKeys.getAppKey());
                return false;
            }
            try {
                notifyUrlValidator.validate(notifyUrl);
                return true;
            } catch (Exception e) {
                log.warn("[事件触发] 订阅方回调地址非法，跳过：eventCode={}, appKey={}, url={}, reason={}",
                        eventCode, appKeys.getAppKey(), notifyUrl,
                        e instanceof OpenException openEx ? openEx.getSubMsg() : e.getMessage());
                return false;
            }
        }).toList();
        if (validAppKeysList.isEmpty()) {
            return;
        }

        List<EventPush> eventPushList = validAppKeysList.stream().map(appKeys -> {
            EventPush eventPush = new EventPush();
            eventPush.setEventCode(eventCode);
            eventPush.setEventTriggerId(triggerId);
            eventPush.setAppId(appKeys.getAppId());
            eventPush.setAppKey(appKeys.getAppKey());
            eventPush.setNotifyUrl(appKeys.getNotifyUrl());
            // 冗余加密配置快照：已生成任务不受后续配置变更影响
            eventPush.setNotifyEncryptionType(appKeys.getNotifyEncryptionType());
            eventPush.setNotifyToken(appKeys.getNotifyToken());
            eventPush.setNotifyEncodingAesKey(appKeys.getNotifyEncodingAesKey());
            eventPush.setRequestData(requestData);
            eventPush.setMaxRequestCnt(retryPolicy.getMaxRetry());
            eventPush.setRequestCnt(0);
            eventPush.setExecStatus(ExecStatusEnum.WAIT.getCode());
            return eventPush;
        }).toList();

        saveBatch(eventPushList);
        List<Long> taskIds = eventPushList.stream().map(EventPush::getId).toList();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notifyTaskDispatcher.submitAll(taskIds, eventPushTaskExecutor);
            }
        });
    }

    @Override
    public Boolean end(Long id) {
        return updateChain().set(EventPush::getExecStatus, ExecStatusEnum.END.getCode())
                .eq(EventPush::getExecStatus, ExecStatusEnum.FAIL.getCode())
                .eq(EventPush::getId, id).update();
    }

    /**
     * 手动立即推送：状态重置 WAIT 后异步投递（修旧版 getById 不判空的 NPE，并保留终态任务重推能力）。
     */
    @Override
    public Long executeImmediately(Long id) {
        EventPush entity = new EventPush();
        entity.setId(id);
        entity.setExecStatus(ExecStatusEnum.WAIT.getCode());
        boolean updated = updateById(entity);
        if (!updated) {
            throw new OpenException(StoryMessageEnum.PARAM_VALIDATION, "事件推送任务不存在：" + id);
        }
        notifyTaskDispatcher.submit(id, eventPushTaskExecutor);
        return id;
    }

    /**
     * 重试扫描（PowerJob 定时调用）：分页取「到期 FAIL」+「丢失 WAIT」两类任务，提交线程池。
     */
    @Override
    public void retry(LocalDateTime now, int limit) {
        ArgumentAssert.notNull(now, "当前时间不能为空");
        LocalDateTime nextTime = now.withSecond(0).withNano(0);
        List<Long> failIds = list(QueryWrapper.create()
                        .select(EventPush::getId)
                        .where(EventPush::getExecStatus).eq(ExecStatusEnum.FAIL.getCode())
                        .and(EventPush::getNextRequestTime).le(nextTime)
                        .orderBy(EventPush::getId, true)
                        .limit(limit))
                .stream().map(EventPush::getId).toList();
        List<Long> lostWaitIds = list(QueryWrapper.create()
                        .select(EventPush::getId)
                        .where(EventPush::getExecStatus).eq(ExecStatusEnum.WAIT.getCode())
                        .and(EventPush::getCreatedAt).le(nextTime.minusMinutes(WAIT_LOST_MINUTES))
                        .orderBy(EventPush::getId, true)
                        .limit(limit))
                .stream().map(EventPush::getId).toList();
        List<Long> taskIds = Stream.concat(failIds.stream(), lostWaitIds.stream()).toList();
        if (taskIds.isEmpty()) {
            log.info("[{}]表无重试记录", EventPush.TABLE_NAME);
            return;
        }
        log.info("[{}]重试扫描命中{}条任务", EventPush.TABLE_NAME, taskIds.size());
        notifyTaskDispatcher.submitAll(taskIds, eventPushTaskExecutor);
    }
}
