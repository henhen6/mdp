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
import top.mddata.console.message.entity.InterfaceStat;
import top.mddata.console.message.query.InterfaceStatQuery;
import top.mddata.console.message.service.InterfaceStatService;
import top.mddata.console.message.vo.InterfaceStatVo;

/**
 * 接口统计 控制层。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@RestController
@Validated
@Tag(name = "接口统计")
@RequestMapping("/message/interfaceStat")
@RequiredArgsConstructor
public class InterfaceStatController extends SuperController<InterfaceStatService, InterfaceStat> {

    /**
     * 根据接口统计主键获取详细信息。
     *
     * @param id 接口统计主键
     * @return 接口统计详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取接口统计")
    @RequestLog("'单体查询:' + #id")
    public R<InterfaceStatVo> get(@RequestParam Long id) {
        InterfaceStat entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, InterfaceStatVo.class));
    }

    /**
     * 分页查询接口统计。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询接口统计")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<InterfaceStatVo>> page(@RequestBody @Validated PageParams<InterfaceStatQuery> params) {
        Page<InterfaceStatVo> page = Page.of(params.getCurrent(), params.getSize());
        InterfaceStat entity = BeanUtil.toBean(params.getModel(), InterfaceStat.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, InterfaceStatVo.class);
        return R.success(page);
    }

}
