package top.mddata.open.service.admin.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.gitee.sop.support.aes.AesException;
import com.gitee.sop.support.aes.MdpBizMsgCrypt;
import com.gitee.sop.support.exception.OpenException;
import com.gitee.sop.support.message.ApiResponse;
import com.google.common.collect.Lists;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.util.StrPool;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.enumeration.StoryMessageEnum;
import top.mddata.open.dto.admin.NotifyInfoDto;
import top.mddata.open.entity.admin.AppKeys;
import top.mddata.open.entity.admin.NotifyInfo;
import top.mddata.open.entity.admin.NotifyInfoLog;
import top.mddata.open.enumeration.admin.EventTypeEnum;
import top.mddata.open.enumeration.admin.ExecStatusEnum;
import top.mddata.open.enumeration.admin.NotifyEncryptionTypeEnum;
import top.mddata.open.mapper.admin.NotifyInfoMapper;
import top.mddata.open.service.admin.AppKeysService;
import top.mddata.open.service.admin.NotifyInfoLogService;
import top.mddata.open.service.admin.NotifyInfoService;
import top.mddata.open.service.admin.processor.MultiThreadTaskProcessor;
import top.mddata.open.service.admin.properties.NotifyProperties;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 回调任务 服务层实现。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@Service
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(NotifyProperties.class)
public class NotifyInfoServiceImpl extends SuperServiceImpl<NotifyInfoMapper, NotifyInfo> implements NotifyInfoService {
    private final NotifyInfoLogService notifyInfoLogService;
    private final AppKeysService appKeysService;
    private final NotifyProperties notifyProperties;
    private final MultiThreadTaskProcessor processor;

    @Override
    public Boolean push(Long id, String url) {
        if (StrUtil.isNotBlank(url)) {
            NotifyInfo entity = new NotifyInfo();
            entity.setNotifyUrl(url).setId(id);
            updateById(entity);
        }
        Long notifyId = notifyImmediately(id);
        log.info("重新推送结果, notifyId={}", notifyId);
        return true;
    }

    @Override
    public Boolean end(Long id) {
        return updateChain().set(NotifyInfo::getExecStatus, ExecStatusEnum.END.getCode())
                .eq(NotifyInfo::getExecStatus, ExecStatusEnum.FAIL.getCode())
                .eq(NotifyInfo::getId, id).update();
    }

    @Override
    public void retry(LocalDateTime now) {
        ArgumentAssert.notNull(now, "当前时间不能为空");
        LocalDateTime nextTime = now.withSecond(0).withNano(0);
        List<NotifyInfo> tasks = list(QueryWrapper.create()
                .le(NotifyInfo::getNextRequestTime, nextTime)
                .eq(NotifyInfo::getExecStatus, ExecStatusEnum.FAIL.getCode()));

        if (CollUtil.isEmpty(tasks)) {
            log.info("[{}]表无重试记录", NotifyInfo.TABLE_NAME);
            return;
        }

        List<List<MultiThreadTaskProcessor.Task>> allTasks = new ArrayList<>();
        int numberOfThreads = notifyProperties.getThreads();
        int tasksPerList = notifyProperties.getThreadTasks();
        log.info("启用{}线程，每个线程有{}个任务", numberOfThreads, tasksPerList);

        List<List<NotifyInfo>> partition = Lists.partition(tasks, tasksPerList);
        int totalLists = partition.size();
        log.info("总共{}个重试任务, 拆分为{}, 每个list有{}个任务, 使用{}个线程", tasks.size(), totalLists, tasksPerList, numberOfThreads);

        for (List<NotifyInfo> subTasks : partition) {
            List<MultiThreadTaskProcessor.Task> list = new ArrayList<>();
            for (NotifyInfo subTask : subTasks) {
                list.add(() -> retryTask(subTask));
            }
            allTasks.add(list);
        }

        processor.processTasksAsync(allTasks, numberOfThreads);
        log.info("任务已提交");
    }

    private void retryTask(NotifyInfo notifyInfo) {
        try {
            log.info("[回调] 开始重试, notifyId={}", notifyInfo.getId());
            if (Objects.equals(notifyInfo.getExecStatus(), ExecStatusEnum.RETRY_OVER.getCode())) {
                log.warn("[回调] 重试次数已用尽, notifyId={}", notifyInfo.getId());
                return;
            }
            doNotify(notifyInfo);
        } catch (Exception e) {
            log.error("[回调] 重试异常，notifyId={}", notifyInfo.getId(), e);
            throw new OpenException(StoryMessageEnum.PARAM_VALIDATION, "重试失败：" + e.getMessage());
        }
    }

    @Override
    public R<Long> notify(NotifyInfoDto request) {
        try {
            NotifyInfo notifyInfo = buildRecord(request);
            return R.success(doNotify(notifyInfo));
        } catch (Exception e) {
            log.error("[回调] 回调异常, request={}", request, e);
            return R.fail(e.getMessage());
        }
    }

    @Override
    public Long notifyImmediately(Long notifyId) {
        NotifyInfo notifyInfo = getById(notifyId);
        try {
            return doNotify(notifyInfo);
        } catch (Exception e) {
            log.error("[回调] 立即推送异常, notifyId={}", notifyId, e);
            throw new OpenException(StoryMessageEnum.PARAM_VALIDATION, "回调失败：" + e.getMessage());
        }
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
        notifyInfo.setMaxRequestCnt(notifyProperties.getMaxRetry());
        notifyInfo.setExecStatus(ExecStatusEnum.WAIT.getCode());
        notifyInfo.setRemark(request.getRemark());

        // 从 AppKeys 冗余加密配置
        AppKeys appKeys = appKeysService.getByAppId(request.getAppId());
        if (appKeys != null) {
            notifyInfo.setNotifyEncryptionType(appKeys.getNotifyEncryptionType());
            notifyInfo.setNotifyToken(appKeys.getNotifyToken());
            notifyInfo.setNotifyEncodingAesKey(appKeys.getNotifyEncodingAesKey());
            // 如果请求中未指定 notifyUrl，使用 AppKeys 中的
            if (StrUtil.isBlank(notifyInfo.getNotifyUrl())) {
                notifyInfo.setNotifyUrl(appKeys.getNotifyUrl());
            }
        }

        return notifyInfo;
    }

    /**
     * 执行回调推送，根据加密模式构建不同的请求体和签名
     */
    private Long doNotify(NotifyInfo notifyInfo) {
        notifyInfo.setRequestCnt(notifyInfo.getRequestCnt() + 1);
        notifyInfo.setLastRequestTime(LocalDateTime.now());

        NotifyInfoLog notifyLog = new NotifyInfoLog();
        String notifyUrl = notifyInfo.getNotifyUrl();

        if (StrUtil.isBlank(notifyUrl)) {
            throw new OpenException(StoryMessageEnum.PARAM_VALIDATION, "回调接口不能为空");
        }

        // 解析原始请求数据
        NotifyInfoDto request = JSON.parseObject(notifyInfo.getRequestData(), NotifyInfoDto.class);

        // 业务数据（字段名小写驼峰）
        JSONObject bizData = new JSONObject();
        bizData.put("type", EventTypeEnum.CALLBACK.name());
        bizData.put("method", notifyInfo.getApiName());
        bizData.put("appKey", notifyInfo.getAppKey());
        bizData.put("version", notifyInfo.getApiVersion());
        bizData.put("timestamp", MdpBizMsgCrypt.generateTimestamp());
        bizData.put("callLogId", notifyInfo.getCallLogId());
        bizData.put("bizContent", request != null ? request.getBizParams() : null);
        String plaintext = bizData.toJSONString();

        // 根据加密模式构建请求体和URL
        int encryptionMode = notifyInfo.getNotifyEncryptionType() != null
                ? notifyInfo.getNotifyEncryptionType()
                : NotifyEncryptionTypeEnum.PLAINTEXT.getCode();

        String requestUrl;
        String requestBody;
        try {
            MdpBizMsgCrypt crypt = new MdpBizMsgCrypt(
                    notifyInfo.getNotifyToken(),
                    notifyInfo.getNotifyEncodingAesKey(),
                    notifyInfo.getAppKey());
            String timestamp = MdpBizMsgCrypt.generateTimestamp();
            String nonce = MdpBizMsgCrypt.generateNonce();
            JSONObject body = crypt.encryptMsg(plaintext, timestamp, nonce, encryptionMode);
            requestBody = body.toJSONString();
            requestUrl = appendUrlParams(notifyUrl,
                    crypt.getSignature(), crypt.getMsgSignature(),
                    timestamp, nonce,
                    encryptionMode == NotifyEncryptionTypeEnum.PLAINTEXT.getCode() ? null : "aes");
        } catch (AesException e) {
            throw new OpenException(StoryMessageEnum.PARAM_VALIDATION, "消息加密失败：" + e.getMessage());
        }

        notifyLog.setRequestData(requestBody);
        log.info("发送回调请求，url={}, content={}", requestUrl, requestBody);
        notifyLog.setRequestTime(LocalDateTime.now());

        try (HttpResponse responseResult = HttpRequest.post(requestUrl)
                .timeout(notifyProperties.getTimeout())
                .body(requestBody)
                .execute()) {
            notifyLog.setResponseTime(LocalDateTime.now());
            String resultContent = responseResult.body();
            notifyLog.setResponseData(resultContent);

            ApiResponse r = JSON.parseObject(resultContent, ApiResponse.class);
            if (ApiResponse.SUCCESS_CODE.equals(r.getCode())) {
                notifyInfo.setExecStatus(ExecStatusEnum.SUCCESS.getCode());
                notifyLog.setErrorMsg(StrPool.EMPTY);
            } else {
                log.error("[回调] 返回数据异常 result={}, code={}", resultContent, r.getCode());
                throw new OpenException(StoryMessageEnum.PARAM_VALIDATION, resultContent);
            }
        } catch (Exception e) {
            notifyLog.setResponseTime(LocalDateTime.now());
            log.error("[回调] 回调请求失败, notifyUrl={}, notifyId={}", notifyUrl, notifyInfo.getId(), e);
            notifyInfo.setExecStatus(ExecStatusEnum.FAIL.getCode());
            if (e instanceof JSONException) {
                notifyLog.setErrorMsg("返回值格式错误，无法解析为 ApiResponse.class，请返回正确格式：{\"code\": 0, \"msg\": \"\"}");
            } else if (e instanceof OpenException openEx) {
                notifyLog.setErrorMsg(openEx.getSubMsg());
            } else {
                notifyLog.setErrorMsg(e.getMessage());
            }
            LocalDateTime nextRequestTime = buildNextSendTime(notifyInfo.getRequestCnt());
            notifyInfo.setNextRequestTime(nextRequestTime);
            if (nextRequestTime == null) {
                log.error("[回调] 回调请求次数达到上限, notifyUrl={}, notifyId={}", notifyUrl, notifyInfo.getId());
                notifyInfo.setExecStatus(ExecStatusEnum.RETRY_OVER.getCode());
            }
        } finally {
            saveOrUpdate(notifyInfo);
            notifyLog.setExecStatus(notifyInfo.getExecStatus());
            notifyLog.setNotifyInfoId(notifyInfo.getId());
            notifyInfoLogService.save(notifyLog);
        }

        return notifyInfo.getId();
    }

    /**
     * 拼接URL参数（小写驼峰命名）
     */
    private String appendUrlParams(String baseUrl, String signature, String msgSignature,
                                   String timestamp, String nonce, String encryptType) {
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append(baseUrl.contains("?") ? "&" : "?");
        if (StrUtil.isNotBlank(signature)) {
            sb.append("signature=").append(signature).append("&");
        }
        if (StrUtil.isNotBlank(msgSignature)) {
            sb.append("msgSignature=").append(msgSignature).append("&");
        }
        sb.append("timestamp=").append(timestamp).append("&");
        sb.append("nonce=").append(nonce);
        if (StrUtil.isNotBlank(encryptType)) {
            sb.append("&encryptType=").append(encryptType);
        }
        return sb.toString();
    }

    /**
     * 构建下一次重试时间
     *
     * @param currentSendCnt 当前发送次数
     * @return 返回null表示重试次数用完
     */
    private LocalDateTime buildNextSendTime(Integer currentSendCnt) {
        String[] split = notifyProperties.getTimeLevel().split(",");
        if (currentSendCnt >= split.length) {
            return null;
        }
        String exp = split[currentSendCnt - 1];
        LocalDateTime time = LocalDateTime.now().withSecond(0).withNano(0);
        char ch = exp.charAt(exp.length() - 1);
        int value = NumberUtils.toInt(exp.substring(0, exp.length() - 1));
        return switch (String.valueOf(ch).toLowerCase()) {
            case "m" -> time.plusMinutes(value);
            case "h" -> time.plusHours(value);
            case "d" -> time.plusDays(value);
            default -> null;
        };
    }
}
