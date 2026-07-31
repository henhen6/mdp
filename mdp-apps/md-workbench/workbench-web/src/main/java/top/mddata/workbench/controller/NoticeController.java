package top.mddata.workbench.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
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
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.base.util.ContextUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.workbench.entity.Notice;
import top.mddata.workbench.entity.NoticeRecipient;
import top.mddata.workbench.query.NoticeQuery;
import top.mddata.workbench.service.NoticeRecipientService;
import top.mddata.workbench.service.NoticeService;
import top.mddata.workbench.vo.NoticeVo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 站内通知 控制层。
 *
 * @author henhen6
 * @since 2025-12-26 09:47:55
 */
@RestController
@Validated
@Tag(name = "站内通知")
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController extends SuperController<NoticeService, Notice> {
    private final NoticeRecipientService noticeRecipientService;

    /**
     * 根据主键删除站内通知。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除站内通知")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 标记为已读。
     *
     * @param ids 主键ID
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/mark")
    @Operation(summary = "标记为已读", description = "标记为已读")
    @RequestLog(value = "标记为已读", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Boolean> mark(@RequestBody List<Long> ids) {
        return R.success(superService.mark(ids, ContextUtil.getUserId()));
    }

    /**
     * 根据站内通知主键获取详细信息。
     *
     * @param id 站内通知主键
     * @return 站内通知详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取站内通知")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<NoticeVo> get(@RequestParam Long id) {
        Notice entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, NoticeVo.class));
    }

    /**
     * 分页查询站内通知。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询站内通知")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<NoticeVo>> page(@RequestBody @Validated PageParams<NoticeQuery> params) {
        Page<NoticeVo> page = Page.of(params.getCurrent(), params.getSize());
        NoticeQuery query = params.getModel();
        Notice entity = BeanUtil.toBean(params.getModel(), Notice.class);

        Long userId = ContextUtil.getUserId();
        ArgumentAssert.notNull(userId, "用户ID不能为空");

        // 主表的所有字段
        Iterable<QueryColumn> queryColumns = QueryMethods.allColumns(Notice.class);
        QueryWrapper wrapper = QueryWrapper.create().select(queryColumns).select(NoticeRecipient::getRead, NoticeRecipient::getReadTime)
                .from(Notice.class).innerJoin(NoticeRecipient.class).on(NoticeRecipient::getNoticeId, Notice::getId)
                .where(NoticeRecipient::getUserId).eq(userId)
                .eq(NoticeRecipient::getRead, query.getRead())
                .eq(Notice::getMsgCategory, query.getMsgCategory())
                .like(Notice::getTitle, query.getTitle())
                .like(Notice::getContent, query.getContent())
                .like(Notice::getUrl, query.getUrl())
                .like(Notice::getAuthor, query.getAuthor());
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, NoticeVo.class);
        return R.success(page);
    }

    /**
     * 批量保存 站内通知接收人
     * @param recipientList 站内通知接收人
     */
    @PostMapping("/saveBatchNoticeRecipient")
    @Operation(hidden = true)
    @RequestLog(value = "批量保存站内通知接收人", logType = RequestLog.LogType.ADD)
    public R<Boolean> saveBatchNoticeRecipient(@RequestBody @Validated List<NoticeRecipient> recipientList) {
        return R.success(noticeRecipientService.saveBatch(recipientList));
    }

    /**
     * 保存 通知
     * @param notice 通知
     */
    @PostMapping("/save")
    @Operation(hidden = true)
    @RequestLog(value = "保存通知", logType = RequestLog.LogType.ADD)
    public R<Boolean> save(@RequestBody @Validated Notice notice) {
        return R.success(superService.save(notice));
    }

    /**
     * 统计指定时间之后、按分类的通知数量
     *
     * @param startTime 开始时间（包含）
     * @param msgCategory 消息分类（1-待办 2-公告 3-预警）
     * @return 通知数量
     */
    @GetMapping("/countByCategory")
    @Operation(hidden = true)
    @RequestLog(value = "统计指定时间之后按分类的通知数量", logType = RequestLog.LogType.QUERY)
    public R<Long> countByCategory(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam Integer msgCategory) {
        return R.success(superService.countByCategory(startTime, msgCategory));
    }

    /**
     * 按分类统计通知数量（用于大屏分类分布图）。
     */
    @GetMapping("/countByCategoryDistribution")
    @Operation(hidden = true)
    @RequestLog(value = "按分类统计通知数量", logType = RequestLog.LogType.QUERY)
    public R<List<Map<String, Object>>> countByCategoryDistribution() {
        return R.success(superService.countByCategoryDistribution());
    }

    /**
     * 统计指定用户的站内通知未读数（用于大屏）。
     */
    @GetMapping("/countUnread")
    @Operation(hidden = true)
    @RequestLog(value = "统计站内通知未读数", logType = RequestLog.LogType.QUERY)
    public R<Long> countUnread(@RequestParam Long userId) {
        return R.success(noticeRecipientService.countUnreadByUserId(userId));
    }

}
