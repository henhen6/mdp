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
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.open.admin.dto.GroupApiRelDto;
import top.mddata.open.admin.entity.GroupApiRel;
import top.mddata.open.admin.query.GroupApiRelQuery;
import top.mddata.open.admin.service.GroupApiRelService;
import top.mddata.open.admin.vo.GroupApiRelVo;

import java.util.List;

/**
 * 分组拥有的对外接口 控制层。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@RestController
@Validated
@Tag(name = "分组拥有的对外接口")
@RequestMapping("//groupApiRel")
@RequiredArgsConstructor
public class GroupApiRelController extends SuperController<GroupApiRelService, GroupApiRel> {
    /**
     * 添加分组拥有的对外接口。
     *
     * @param dto 分组拥有的对外接口
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存分组拥有的对外接口")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody GroupApiRelDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除分组拥有的对外接口。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除分组拥有的对外接口")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新分组拥有的对外接口。
     *
     * @param dto 分组拥有的对外接口
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新分组拥有的对外接口")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody GroupApiRelDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 根据分组拥有的对外接口主键获取详细信息。
     *
     * @param id 分组拥有的对外接口主键
     * @return 分组拥有的对外接口详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取分组拥有的对外接口")
    @RequestLog("'单体查询:' + #id")
    public R<GroupApiRelVo> get(@RequestParam Long id) {
        GroupApiRel entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, GroupApiRelVo.class));
    }

    /**
     * 分页查询分组拥有的对外接口。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询分组拥有的对外接口")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<GroupApiRelVo>> page(@RequestBody @Validated PageParams<GroupApiRelQuery> params) {
        Page<GroupApiRelVo> page = Page.of(params.getCurrent(), params.getSize());
        GroupApiRel entity = BeanUtil.toBean(params.getModel(), GroupApiRel.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, GroupApiRelVo.class);
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
    public R<List<GroupApiRelVo>> list(@RequestBody @Validated GroupApiRelQuery params) {
        GroupApiRel entity = BeanUtil.toBean(params, GroupApiRel.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<GroupApiRelVo> listVo = superService.listAs(wrapper, GroupApiRelVo.class);
        return R.success(listVo);
    }
}
