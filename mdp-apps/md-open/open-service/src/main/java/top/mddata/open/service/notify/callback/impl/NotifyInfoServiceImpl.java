package top.mddata.open.service.notify.callback.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.gitee.sop.support.exception.OpenException;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.enumeration.StoryMessageEnum;
import top.mddata.open.dto.admin.NotifyInfoDto;
import top.mddata.open.entity.admin.AppKeys;
import top.mddata.open.entity.admin.NotifyInfo;
import top.mddata.open.enumeration.admin.ExecStatusEnum;
import top.mddata.open.mapper.admin.NotifyInfoMapper;
import top.mddata.open.service.app.AppKeysService;
import top.mddata.open.service.notify.callback.NotifyInfoService;
import top.mddata.open.service.notify.callback.NotifyInfoTaskExecutor;
import top.mddata.open.service.notify.pipeline.NotifyTaskDispatcher;
import top.mddata.open.service.notify.pipeline.NotifyUrlValidator;
import top.mddata.open.service.notify.pipeline.RetryPolicy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * 回调任务 服务层实现。
 * 投递/重试/日志管道已下沉到 notify.pipeline，本类只负责任务的创建、状态查询与调度触发。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotifyInfoServiceImpl extends SuperServiceImpl<NotifyInfoMapper, NotifyInfo> implements NotifyInfoService {

    /** WAIT 状态超过该分钟数视为「投递丢失」（线程池拒绝/服务重启），由重试扫描兜底 */
    private static final int WAIT_LOST_MINUTES = 5;

    private final AppKeysService appKeysService;
    private final RetryPolicy retryPolicy;
    private final NotifyTaskDispatcher notifyTaskDispatcher;
    private final NotifyInfoTaskExecutor notifyInfoTaskExecutor;
    private final NotifyUrlValidator notifyUrlValidator;

    /**
     * 生成回调任务：事务内只写库，事务提交后再异步投递（修旧版在调用方线程同步发 HTTP 最长阻塞 5s 的问题）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Long> notify(NotifyInfoDto request) {
        NotifyInfo notifyInfo = buildRecord(request);
        notifyUrlValidator.validate(notifyInfo.getNotifyUrl());
        save(notifyInfo);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notifyTaskDispatcher.submit(notifyInfo.getId(), notifyInfoTaskExecutor);
            }
        });
        return R.success(notifyInfo.getId());
    }

    /**
     * 手动重推：状态重置为 WAIT 后异步投递。
     * 重置是为了保留旧版「RETRY_OVER/已结束任务可手动重推」的能力（执行器对终态任务幂等跳过）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean push(Long id, String url) {
        if (StrUtil.isNotBlank(url)) {
            notifyUrlValidator.validate(url);
        }
        NotifyInfo entity = new NotifyInfo();
        entity.setId(id);
        entity.setExecStatus(ExecStatusEnum.WAIT.getCode());
        if (StrUtil.isNotBlank(url)) {
            entity.setNotifyUrl(url);
        }
        boolean updated = updateById(entity);
        if (!updated) {
            throw new OpenException(StoryMessageEnum.PARAM_VALIDATION, "回调任务不存在：" + id);
        }
        // 事务提交后再投递，防止执行器读到提交前旧终态导致幂等跳过
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notifyTaskDispatcher.submit(id, notifyInfoTaskExecutor);
            }
        });
        return true;
    }

    @Override
    public Boolean end(Long id) {
        return updateChain().set(NotifyInfo::getExecStatus, ExecStatusEnum.END.getCode())
                .eq(NotifyInfo::getExecStatus, ExecStatusEnum.FAIL.getCode())
                .eq(NotifyInfo::getId, id).update();
    }

    /**
     * 重试扫描（PowerJob 定时调用）：分页取「到期 FAIL」+「丢失 WAIT」两类任务，提交线程池。
     */
    @Override
    public void retry(LocalDateTime now, int limit) {
        ArgumentAssert.notNull(now, "当前时间不能为空");
        LocalDateTime nextTime = now.withSecond(0).withNano(0);
        List<Long> failIds = list(QueryWrapper.create()
                        .select(NotifyInfo::getId)
                        .where(NotifyInfo::getExecStatus).eq(ExecStatusEnum.FAIL.getCode())
                        .and(NotifyInfo::getNextRequestTime).le(nextTime)
                        .orderBy(NotifyInfo::getId, true)
                        .limit(limit))
                .stream().map(NotifyInfo::getId).toList();
        List<Long> lostWaitIds = list(QueryWrapper.create()
                        .select(NotifyInfo::getId)
                        .where(NotifyInfo::getExecStatus).eq(ExecStatusEnum.WAIT.getCode())
                        .and(NotifyInfo::getCreatedAt).le(nextTime.minusMinutes(WAIT_LOST_MINUTES))
                        .orderBy(NotifyInfo::getId, true)
                        .limit(limit))
                .stream().map(NotifyInfo::getId).toList();
        List<Long> taskIds = Stream.concat(failIds.stream(), lostWaitIds.stream()).toList();
        if (taskIds.isEmpty()) {
            log.info("[{}]表无重试记录", NotifyInfo.TABLE_NAME);
            return;
        }
        log.info("[{}]重试扫描命中{}条任务", NotifyInfo.TABLE_NAME, taskIds.size());
        notifyTaskDispatcher.submitAll(taskIds, notifyInfoTaskExecutor);
    }

    private NotifyInfo buildRecord(NotifyInfoDto request) {
        NotifyInfo notifyInfo = new NotifyInfo();
        notifyInfo.setCallLogId(request.getCallLogId());
        notifyInfo.setAppId(request.getAppId());
        notifyInfo.setAppKey(request.getAppKey());
        notifyInfo.setApiName(request.getApiName());
        notifyInfo.setApiVersion(request.getApiVersion());
        notifyInfo.setNotifyUrl(request.getNotifyUrl());
        notifyInfo.setRequestData(JSON.toJSONString(request));
        notifyInfo.setRequestCnt(0);
        notifyInfo.setMaxRequestCnt(retryPolicy.getMaxRetry());
        notifyInfo.setExecStatus(ExecStatusEnum.WAIT.getCode());
        notifyInfo.setRemark(request.getRemark());

        // 从 AppKeys 冗余加密配置快照：已生成任务不受后续配置变更影响
        AppKeys appKeys = appKeysService.getByAppId(request.getAppId());
        if (appKeys != null) {
            notifyInfo.setNotifyEncryptionType(appKeys.getNotifyEncryptionType());
            notifyInfo.setNotifyToken(appKeys.getNotifyToken());
            notifyInfo.setNotifyEncodingAesKey(appKeys.getNotifyEncodingAesKey());
            if (StrUtil.isBlank(notifyInfo.getNotifyUrl())) {
                notifyInfo.setNotifyUrl(appKeys.getNotifyUrl());
            }
        }
        return notifyInfo;
    }
}
