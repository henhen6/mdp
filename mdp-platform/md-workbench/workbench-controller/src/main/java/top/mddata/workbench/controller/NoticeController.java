package top.mddata.workbench.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
import top.mddata.base.utils.ContextUtil;
import top.mddata.workbench.entity.Notice;
import top.mddata.workbench.entity.NoticeRecipient;
import top.mddata.workbench.query.NoticeQuery;
import top.mddata.workbench.service.NoticeService;
import top.mddata.workbench.vo.NoticeVo;

import java.util.List;

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

    /**
     * 根据主键删除站内通知。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除站内通知")
    @RequestLog("'删除:' + #ids")
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
    @RequestLog(value = "标记为已读", request = false)
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
    @RequestLog("'单体查询:' + #id")
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
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<NoticeVo>> page(@RequestBody @Validated PageParams<NoticeQuery> params) {
        Page<NoticeVo> page = Page.of(params.getCurrent(), params.getSize());
        NoticeQuery query = params.getModel();
        Notice entity = BeanUtil.toBean(params.getModel(), Notice.class);
        // 主表的所有字段
        Iterable<QueryColumn> queryColumns = QueryMethods.allColumns(Notice.class);
        QueryWrapper wrapper = QueryWrapper.create().select(queryColumns).select(NoticeRecipient::getRead, NoticeRecipient::getReadTime)
                .from(Notice.class).innerJoin(NoticeRecipient.class).on(NoticeRecipient::getNoticeId, Notice::getId)
                .where(NoticeRecipient::getUserId).eq(ContextUtil.getUserId())
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


}
