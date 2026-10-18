package top.mddata.workbench.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.workbench.entity.Notice;
import top.mddata.workbench.entity.NoticeRecipient;
import top.mddata.workbench.service.NoticeRecipientService;
import top.mddata.workbench.service.NoticeService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内部-站内通知入口：仅供平台内部服务（console 消息任务等）经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-站内通知")
@RequestMapping("/inner/notice")
@RequiredArgsConstructor
public class InnerNoticeController {

    private final NoticeService noticeService;
    private final NoticeRecipientService noticeRecipientService;

    /**
     * 批量保存 站内通知接收人
     * @param recipientList 站内通知接收人
     */
    @Operation(hidden = true)
    @PostMapping("/saveBatchNoticeRecipient")
    public R<Boolean> saveBatchNoticeRecipient(@RequestBody @Validated List<NoticeRecipient> recipientList) {
        return R.success(noticeRecipientService.saveBatch(recipientList));
    }

    /**
     * 保存 通知
     * @param notice 通知
     */
    @Operation(hidden = true)
    @PostMapping("/save")
    public R<Boolean> save(@RequestBody @Validated Notice notice) {
        return R.success(noticeService.save(notice));
    }

    /**
     * 统计指定时间之后、按分类的通知数量
     *
     * @param startTime 开始时间（包含）
     * @param msgCategory 消息分类（1-待办 2-公告 3-预警）
     * @return 通知数量
     */
    @Operation(hidden = true)
    @GetMapping("/countByCategory")
    public R<Long> countByCategory(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam Integer msgCategory) {
        return R.success(noticeService.countByCategory(startTime, msgCategory));
    }

    /**
     * 统计指定用户的站内通知未读数
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @Operation(hidden = true)
    @GetMapping("/countUnread")
    public R<Long> countUnread(@RequestParam Long userId) {
        return R.success(noticeRecipientService.countUnreadByUserId(userId));
    }
}
