package top.mddata.workbench.facade;

import top.mddata.workbench.entity.Notice;
import top.mddata.workbench.entity.NoticeRecipient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知 接口
 * @author henhen6
 * @since 2025/7/27 00:39
 */
public interface NoticeFacade {

    /**
     * 批量保存 站内通知接收人
     * @param recipientList 站内通知接收人
     */
    void saveBatchNoticeRecipient(List<NoticeRecipient> recipientList);

    /**
     * 保存 通知
     * @param notice 通知
     */
    void save(Notice notice);

    /**
     * 统计指定时间之后、按分类的通知数量
     *
     * @param startTime 开始时间（包含）
     * @param msgCategory 消息分类（1-待办 2-公告 3-预警）
     * @return 通知数量
     */
    Long countByCategory(LocalDateTime startTime, Integer msgCategory);

    /**
     * 按分类统计通知数量（用于大屏分类分布图）。
     *
     * @return key=msgCategory、value=count
     */
    List<Map<String, Object>> countByCategoryDistribution();

    /**
     * 统计当前用户的站内通知未读数。
     *
     * @return 未读通知数量
     */
    Long countUnread();
}