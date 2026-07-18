package top.mddata.console.controller.system;

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
import top.mddata.console.dto.system.RequestLogDto;
import top.mddata.console.entity.system.RequestLogDetail;
import top.mddata.console.query.system.RequestLogQuery;
import top.mddata.console.service.system.RequestLogDetailService;
import top.mddata.console.service.system.RequestLogService;
import top.mddata.console.vo.system.RequestLogVo;

import java.util.List;

/**
 * 请求日志 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 20:06:39
 */
@RestController
@Validated
@Tag(name = "请求日志")
@RequestMapping("/system/requestLog")
@RequiredArgsConstructor
public class RequestLogController extends SuperController<RequestLogService, top.mddata.console.entity.system.RequestLog> {
    private final RequestLogDetailService requestLogDetailService;

    /**
     * 添加请求日志。
     *
     * @param dto 请求日志
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存请求日志")
    @RequestLog(value = "新增", logType = RequestLog.LogType.ADD)
    public R<Long> save(@Validated @RequestBody RequestLogDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除请求日志。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除请求日志")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }


    /**
     * 根据请求日志主键获取详细信息。
     *
     * @param id 请求日志主键
     * @return 请求日志详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取请求日志")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<RequestLogVo> get(@RequestParam Long id) {
        top.mddata.console.entity.system.RequestLog entity = superService.getById(id);
        RequestLogVo vo = BeanUtil.toBean(entity, RequestLogVo.class);
        RequestLogDetail detail = requestLogDetailService.getById(id);
        BeanUtil.copyProperties(detail, vo);
        return R.success(vo);
    }

    /**
     * 分页查询请求日志。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询请求日志")
    public R<Page<RequestLogVo>> page(@RequestBody @Validated PageParams<RequestLogQuery> params) {
        Page<RequestLogVo> page = Page.of(params.getCurrent(), params.getSize());
        top.mddata.console.entity.system.RequestLog entity = BeanUtil.toBean(params.getModel(), top.mddata.console.entity.system.RequestLog.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, RequestLogVo.class);
        return R.success(page);
    }

}
