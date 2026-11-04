package top.mddata.worker.processors;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.console.service.message.strategy.MsgTaskContext;

import java.time.Duration;

/**
 * 定时消息任务发送：PowerJob 延迟实例到点触发（instanceParams = 消息任务ID）。
 *
 * <p>链路：MsgTaskServiceImpl.submitTimingJob 经 OpenAPI runJob 创建延迟实例，
 * 本 processor 到点执行发送（与即时发送共用 MsgTaskContext.execSend 链路）。
 *
 * <p>配套常驻 job 在 PowerJob 控制台创建一次：
 * timeExpressionType=API、processor=本类全限定名，jobId 配置于 powerjob.server.msg-job-id。
 *
 * <p>幂等：InstanceStatusChecker 超时重投可能造成双触发，执行前用 CacheOps.setIfAbsent
 * 占位（发送失败时释放占位以支持 PowerJob 手动重试 instance）。
 *
 * @author henhen6
 * @since 2026-10-31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MsgTaskSendProcessor implements BasicProcessor {

    /** 发送占位 key（防 StatusChecker 重投导致重复发送），1 小时后自动过期 */
    private static final String GRAB_KEY_PREFIX = "mdp:msg:send:grab:";

    private final MsgTaskContext msgTaskContext;
    private final CacheOps cacheOps;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessResult process(TaskContext context) {
        String msgTaskIdStr = StrUtil.blankToDefault(context.getInstanceParams(), context.getJobParams());
        if (StrUtil.isBlank(msgTaskIdStr)) {
            return new ProcessResult(false, "instanceParams 为空，缺少消息任务ID");
        }
        long msgTaskId;
        try {
            msgTaskId = Long.parseLong(msgTaskIdStr.trim());
        } catch (NumberFormatException e) {
            return new ProcessResult(false, "消息任务ID不合法: " + msgTaskIdStr);
        }

        // SETNX 占位：重投的双触发只有一个能进入发送流程
        Boolean grabbed = cacheOps.setIfAbsent(new CacheKey(GRAB_KEY_PREFIX + msgTaskId, Duration.ofHours(1)), "1");
        if (!Boolean.TRUE.equals(grabbed)) {
            log.warn("[MsgTaskSend] 消息任务[{}]已在发送流程中，本次触发跳过", msgTaskId);
            return new ProcessResult(true, "消息任务已在发送流程中，跳过重复触发");
        }

        try {
            msgTaskContext.execSend(msgTaskId);
            return new ProcessResult(true, "定时消息发送完成: " + msgTaskId);
        } catch (Exception e) {
            // 释放占位，允许通过 PowerJob 重试 instance 重新发送
            cacheOps.del(GRAB_KEY_PREFIX + msgTaskId);
            log.error("[MsgTaskSend] 定时消息发送失败, msgTaskId={}", msgTaskId, e);
            throw e;
        }
    }
}
