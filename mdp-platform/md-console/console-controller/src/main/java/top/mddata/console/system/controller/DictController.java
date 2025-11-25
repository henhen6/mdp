package top.mddata.console.system.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
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
import top.mddata.console.system.dto.DictDto;
import top.mddata.console.system.entity.Dict;
import top.mddata.console.system.query.DictQuery;
import top.mddata.console.system.service.DictService;
import top.mddata.console.system.vo.DictVo;

import java.io.Serializable;
import java.util.List;

import static top.mddata.common.constant.SwaggerConstants.DATA_TYPE_LONG;
import static top.mddata.common.constant.SwaggerConstants.DATA_TYPE_STRING;

/**
 * 字典 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 20:06:39
 */
@RestController
@Validated
@Tag(name = "字典")
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class DictController extends SuperController<DictService, Dict> {
    /**
     * 添加字典。
     *
     * @param dto 字典
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存字典")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody DictDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除字典。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除字典")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新字典。
     *
     * @param dto 字典
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新字典")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody DictDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 分页查询字典。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询字典")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<DictVo>> page(@RequestBody @Validated PageParams<DictQuery> params) {
        Page<DictVo> page = Page.of(params.getCurrent(), params.getSize());
        Dict entity = BeanUtil.toBean(params.getModel(), Dict.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, DictVo.class);
        return R.success(page);
    }

    /**
     * 批量查询
     * @param params 查询参数
     * @return 集合
     */
    @PostMapping("/list")
    @Operation(summary = "批量查询", description = "批量查询")
    @RequestLog(value = "批量查询", response = false)
    public R<List<DictVo>> list(@RequestBody @Validated DictQuery params) {
        Dict entity = BeanUtil.toBean(params, Dict.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<DictVo> listVo = superService.listAs(wrapper, DictVo.class);
        return R.success(listVo);
    }

    @Parameters({
            @Parameter(name = "id", description = "ID", schema = @Schema(type = DATA_TYPE_LONG), in = ParameterIn.QUERY),
            @Parameter(name = "uniqKey", description = "字典标识", schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
    })
    @Operation(summary = "检测字典标识是否可用", description = "检测字典标识是否可用")
    @GetMapping("/check")
    @RequestLog("检测字典标识是否可用")
    public R<Boolean> check(@RequestParam String uniqKey, @RequestParam(required = false) Long id) {
        return R.success(superService.checkByUniqKey(uniqKey, id));
    }

    @Operation(summary = "通过枚举导入字典", description = "通过枚举导入字典")
    @PostMapping("/importDictByEnum")
    @RequestLog("通过枚举导入字典")
    public R<Boolean> importDictByEnum(@RequestBody List<DictVo> list) {
        return R.success(superService.importDictByEnum(list));
    }

    @Operation(summary = "清空缓存", description = "清空缓存")
    @PostMapping("/clearCache")
    @RequestLog("清空缓存")
    public R<Boolean> clearCache(@RequestBody List<Serializable> list) {
        superService.clearCache(list);
        return R.success();
    }
}
