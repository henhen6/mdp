package top.mddata.open.controller.admin;

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
import top.mddata.open.entity.admin.NotifyInfoLog;
import top.mddata.open.query.admin.NotifyInfoLogQuery;
import top.mddata.open.service.admin.NotifyInfoLogService;
import top.mddata.open.vo.admin.NotifyInfoLogVo;

import java.util.List;

/**
 * 回调任务日志 控制层。
 *
 * @author henhen6
 * @since 2026-01-12 21:29:13
 */
@RestController
@Validated
@Tag(name = "回调任务日志")
@RequestMapping("/admin/notifyInfoLog")
@RequiredArgsConstructor
public class NotifyInfoLogController extends SuperController<NotifyInfoLogService, NotifyInfoLog> {

    /**
     * 根据主键删除回调任务日志。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除回调任务日志")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据回调任务日志主键获取详细信息。
     *
     * @param id 回调任务日志主键
     * @return 回调任务日志详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取回调任务日志")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<NotifyInfoLogVo> get(@RequestParam Long id) {
        NotifyInfoLog entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, NotifyInfoLogVo.class));
    }

    /**
     * 分页查询回调任务日志。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询回调任务日志")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<NotifyInfoLogVo>> page(@RequestBody @Validated PageParams<NotifyInfoLogQuery> params) {
        Page<NotifyInfoLogVo> page = Page.of(params.getCurrent(), params.getSize());
        NotifyInfoLog entity = BeanUtil.toBean(params.getModel(), NotifyInfoLog.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, NotifyInfoLogVo.class);
        return R.success(page);
    }

    /**
     * 批量查询
     * @param params 查询参数
     * @return 集合
     */
    @PostMapping("/list")
    @Operation(summary = "批量查询", description = "批量查询")
    @RequestLog(value = "批量查询", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<NotifyInfoLogVo>> list(@RequestBody @Validated NotifyInfoLogQuery params) {
        NotifyInfoLog entity = BeanUtil.toBean(params, NotifyInfoLog.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<NotifyInfoLogVo> listVo = superService.listAs(wrapper, NotifyInfoLogVo.class);
        return R.success(listVo);
    }
}
