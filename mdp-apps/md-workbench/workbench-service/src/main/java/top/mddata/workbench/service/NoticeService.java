package top.mddata.workbench.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.workbench.entity.Notice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 站内通知 服务层。
 *
 * @author henhen6
 * @since 2025-12-26 09:47:55
 */
public interface NoticeService extends SuperService<Notice> {

    /**
     * 标记已读。
     *
     * @param ids 通知id
     * @param userId 用户id
     * @return 是否成功
     */
    Boolean mark(List<Long> ids, Long userId);

    /**
     * 统计指定时间之后、按分类的通知数量
     *
     * @param startTime 开始时间
     * @param msgCategory 消息分类（1-待办 2-公告 3-预警）
     * @return 数量
     */
    Long countByCategory(LocalDateTime startTime, Integer msgCategory);

    /**
     * 按分类统计通知数量（用于大屏分类分布图）。
     *
     * @return key=msgCategory、value=count
     */
    List<Map<String, Object>> countByCategoryDistribution();
}