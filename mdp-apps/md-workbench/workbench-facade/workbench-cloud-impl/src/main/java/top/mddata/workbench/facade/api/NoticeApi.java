package top.mddata.workbench.facade.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.workbench.entity.Notice;
import top.mddata.workbench.entity.NoticeRecipient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 跨服务-通知实现类
 * @author henhen6
 * @since 2025/12/26 10:21
 */
@FeignClient(name = AppConstants.WORKBENCH_SERVER, path = "/notice")
public interface NoticeApi {

    /**
     * 批量保存 站内通知接收人
     * @param recipientList 站内通知接收人
     */
    @PostMapping("/saveBatchNoticeRecipient")
    R<Boolean> saveBatchNoticeRecipient(List<NoticeRecipient> recipientList);

    /**
     * 保存 通知
     * @param notice 通知
     */
    @PostMapping("/save")
    R<Boolean> save(Notice notice);

    /**
     * 统计指定时间之后、按分类的通知数量
     *
     * @param startTime 开始时间（包含）
     * @param msgCategory 消息分类（1-待办 2-公告 3-预警）
     * @return 通知数量
     */
    @GetMapping("/countByCategory")
    R<Long> countByCategory(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam Integer msgCategory);

    /**
     * 按分类统计通知数量（用于大屏分类分布图）。
     *
     * @return key=msgCategory、value=count
     */
    @GetMapping("/countByCategoryDistribution")
    R<List<Map<String, Object>>> countByCategoryDistribution();

    /**
     * 统计当前用户的站内通知未读数。
     *
     * @param userId 用户ID（从请求头获取）
     * @return 未读通知数量
     */
    @GetMapping("/countUnread")
    R<Long> countUnread(@RequestParam Long userId);
}