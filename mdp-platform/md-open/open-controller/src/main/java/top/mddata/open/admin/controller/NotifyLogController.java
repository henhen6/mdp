package top.mddata.open.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
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
import top.mddata.open.admin.entity.NotifyLog;
import top.mddata.open.admin.query.NotifyLogQuery;
import top.mddata.open.admin.service.NotifyLogService;
import top.mddata.open.admin.vo.NotifyLogVo;

import java.util.List;

/**
 * 回调日志 控制层。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@RestController
@Validated
@Tag(name = "回调日志")
@RequestMapping("/admin/notifyLog")
@RequiredArgsConstructor
public class NotifyLogController extends SuperController<NotifyLogService, NotifyLog> {

    /**
     * 根据主键删除回调日志。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除回调日志")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }


    /**
     * 根据回调日志主键获取详细信息。
     *
     * @param id 回调日志主键
     * @return 回调日志详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取回调日志")
    @RequestLog("'单体查询:' + #id")
    public R<NotifyLogVo> get(@RequestParam Long id) {
        NotifyLog entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, NotifyLogVo.class));
    }

    /**
     * 分页查询回调日志。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询回调日志")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<NotifyLogVo>> page(@RequestBody @Validated PageParams<NotifyLogQuery> params) {
        Page<NotifyLogVo> page = Page.of(params.getCurrent(), params.getSize());
        NotifyLog entity = BeanUtil.toBean(params.getModel(), NotifyLog.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, NotifyLogVo.class);
        return R.success(page);
    }

}
