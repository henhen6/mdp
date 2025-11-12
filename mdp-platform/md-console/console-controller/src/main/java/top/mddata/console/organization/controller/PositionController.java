package top.mddata.console.organization.controller;

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
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.common.entity.Position;
import top.mddata.console.organization.dto.PositionDto;
import top.mddata.console.organization.query.PositionQuery;
import top.mddata.console.organization.service.PositionService;
import top.mddata.console.organization.vo.PositionVo;

import java.util.List;

/**
 * 岗位 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 15:48:54
 */
@RestController
@Validated
@Tag(name = "岗位")
@RequestMapping("/organization/position")
@RequiredArgsConstructor
public class PositionController extends SuperController<PositionService, Position> {
    private final PositionService positionService;

    /**
     * 添加岗位。
     *
     * @param dto 岗位
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存岗位")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody PositionDto dto) {
        return R.success(positionService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除岗位。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除岗位")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(positionService.removeByIds(ids));
    }

    /**
     * 根据主键更新岗位。
     *
     * @param dto 岗位
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新岗位")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody PositionDto dto) {
        return R.success(positionService.updateDtoById(dto).getId());
    }

    /**
     * 根据岗位主键获取详细信息。
     *
     * @param id 岗位主键
     * @return 岗位详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取岗位")
    @RequestLog("'单体查询:' + #id")
    public R<PositionVo> get(@RequestParam Long id) {
        Position entity = positionService.getById(id);
        return R.success(BeanUtil.toBean(entity, PositionVo.class));
    }

    /**
     * 分页查询岗位。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询岗位")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<PositionVo>> page(@RequestBody @Validated PageParams<PositionQuery> params) {
        Page<PositionVo> page = Page.of(params.getCurrent(), params.getSize());
        Position entity = BeanUtil.toBean(params.getModel(), Position.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        positionService.pageAs(page, wrapper, PositionVo.class);
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
    public R<List<PositionVo>> list(@RequestBody @Validated PositionQuery params) {
        Position entity = BeanUtil.toBean(params, Position.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<PositionVo> listVo = positionService.listAs(wrapper, PositionVo.class);
        return R.success(listVo);
    }
}
