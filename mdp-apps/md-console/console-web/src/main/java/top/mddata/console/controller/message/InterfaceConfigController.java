package top.mddata.console.controller.message;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
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
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.common.vo.Option;
import top.mddata.console.dto.message.InterfaceConfigDto;
import top.mddata.console.dto.message.InterfaceConfigSettingDto;
import top.mddata.console.entity.message.InterfaceConfig;
import top.mddata.console.query.message.InterfaceConfigQuery;
import top.mddata.console.service.message.InterfaceConfigService;
import top.mddata.console.service.message.strategy.MsgTaskStrategy;
import top.mddata.console.vo.message.InterfaceConfigVo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 接口 控制层。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@RestController
@Validated
@Tag(name = "接口")
@RequestMapping("/message/interfaceConfig")
@RequiredArgsConstructor
public class InterfaceConfigController extends SuperController<InterfaceConfigService, InterfaceConfig> {
    /**
     * 添加接口。
     *
     * @param dto 接口
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存接口")
    @RequestLog(value = "新增", logType = RequestLog.LogType.ADD, request = false)
    public R<Long> save(@Validated @RequestBody InterfaceConfigDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除接口。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除接口")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新接口。
     *
     * @param dto 接口
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新接口")
    @RequestLog(value = "修改", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody InterfaceConfigDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 修改配置。
     *
     * @param dto 接口
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/updateConfig")
    @Operation(summary = "修改配置", description = "根据主键修改配置")
    @RequestLog(value = "修改配置", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Long> updateConfig(@RequestBody InterfaceConfigSettingDto dto) {
        return R.success(superService.updateConfigById(dto));
    }

    /**
     * 根据接口主键获取详细信息。
     *
     * @param id 接口主键
     * @return 接口详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取接口")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<InterfaceConfigVo> get(@RequestParam Long id) {
        InterfaceConfig entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, InterfaceConfigVo.class));
    }

    /**
     * 分页查询接口。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询接口")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<InterfaceConfigVo>> page(@RequestBody @Validated PageParams<InterfaceConfigQuery> params) {
        Page<InterfaceConfigVo> page = Page.of(params.getCurrent(), params.getSize());
        InterfaceConfig entity = BeanUtil.toBean(params.getModel(), InterfaceConfig.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, InterfaceConfigVo.class);
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
    public R<List<InterfaceConfigVo>> list(@RequestBody @Validated InterfaceConfigQuery params) {
        InterfaceConfig entity = BeanUtil.toBean(params, InterfaceConfig.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<InterfaceConfigVo> listVo = superService.listAs(wrapper, InterfaceConfigVo.class);
        return R.success(listVo);
    }

    @PostMapping("/listImplClass")
    @Operation(summary = "获取接口实现类", description = "获取系统中存在的接口实现类")
    @RequestLog(value = "获取接口实现类", logType = RequestLog.LogType.QUERY, response = false)
    public R<List<Option>> listImplClass() {
        Map<String, MsgTaskStrategy> beansOfType = SpringUtil.getBeansOfType(MsgTaskStrategy.class);
        List<Option> list = new ArrayList<>();
        beansOfType.forEach((key, value) -> {
            list.add(Option.builder().value(key).label(value.getClass().getSimpleName()).build());
        });
        return R.success(list);
    }
}
