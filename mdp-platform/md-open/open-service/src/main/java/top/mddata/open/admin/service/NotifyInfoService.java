package top.mddata.open.admin.service;

import top.mddata.base.base.R;
import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.admin.dto.NotifyRequest;
import top.mddata.open.admin.entity.NotifyInfo;

import java.time.LocalDateTime;

/**
 * 回调任务 服务层。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
public interface NotifyInfoService extends SuperService<NotifyInfo> {
    /**
     * 回调重试
     * @param now 当前时间
     */
    void retry(LocalDateTime now);

    /**
     * 第一次回调
     *
     * @param request 回调内容
     * @return 返回回调id
     */
    R<Long> notify(NotifyRequest request);

    /**
     * 立即执行回调
     *
     * @param notifyId notifyId
     * @return 返回结果
     */
    Long notifyImmediately(Long notifyId);

    /**
     * 手动执行通知
     * @param id 任务id
     * @param url 回调地址
     * @return 任务id
     */
    Boolean push(Long id, String url);

    /**
     * 结束重试
     * @param id 任务id
     * @return 是否成功
     */
    Boolean end(Long id);
}
