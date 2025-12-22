package top.mddata.console.message.controller;

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
import top.mddata.console.message.entity.InterfaceLog;
import top.mddata.console.message.query.InterfaceLogQuery;
import top.mddata.console.message.service.InterfaceLogService;
import top.mddata.console.message.vo.InterfaceLogVo;

import java.util.List;

/**
 * 接口执行日志记录 控制层。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@RestController
@Validated
@Tag(name = "接口执行日志记录")
@RequestMapping("/message/interfaceLog")
@RequiredArgsConstructor
public class InterfaceLogController extends SuperController<InterfaceLogService, InterfaceLog> {

    /**
     * 根据主键删除接口执行日志记录。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除接口执行日志记录")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }


    /**
     * 根据接口执行日志记录主键获取详细信息。
     *
     * @param id 接口执行日志记录主键
     * @return 接口执行日志记录详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取接口执行日志记录")
    @RequestLog("'单体查询:' + #id")
    public R<InterfaceLogVo> get(@RequestParam Long id) {
        InterfaceLog entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, InterfaceLogVo.class));
    }

    /**
     * 分页查询接口执行日志记录。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询接口执行日志记录")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<InterfaceLogVo>> page(@RequestBody @Validated PageParams<InterfaceLogQuery> params) {
        Page<InterfaceLogVo> page = Page.of(params.getCurrent(), params.getSize());
        InterfaceLog entity = BeanUtil.toBean(params.getModel(), InterfaceLog.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, InterfaceLogVo.class);
        return R.success(page);
    }

}
