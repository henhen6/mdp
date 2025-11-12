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
import top.mddata.common.entity.UserRoleRel;
import top.mddata.console.organization.dto.UserRoleRelDto;
import top.mddata.console.organization.query.UserRoleRelQuery;
import top.mddata.console.organization.service.UserRoleRelService;
import top.mddata.console.organization.vo.UserRoleRelVo;

import java.util.List;

/**
 * 用户角色关联 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 15:50:00
 */
@RestController
@Validated
@Tag(name = "用户角色关联")
@RequestMapping("/organization/userRoleRel")
@RequiredArgsConstructor
public class UserRoleRelController extends SuperController<UserRoleRelService, UserRoleRel> {
    private final UserRoleRelService userRoleRelService;

    /**
     * 添加用户角色关联。
     *
     * @param dto 用户角色关联
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存用户角色关联")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody UserRoleRelDto dto) {
        return R.success(userRoleRelService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除用户角色关联。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除用户角色关联")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(userRoleRelService.removeByIds(ids));
    }

    /**
     * 根据主键更新用户角色关联。
     *
     * @param dto 用户角色关联
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新用户角色关联")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody UserRoleRelDto dto) {
        return R.success(userRoleRelService.updateDtoById(dto).getId());
    }

    /**
     * 根据用户角色关联主键获取详细信息。
     *
     * @param id 用户角色关联主键
     * @return 用户角色关联详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取用户角色关联")
    @RequestLog("'单体查询:' + #id")
    public R<UserRoleRelVo> get(@RequestParam Long id) {
        UserRoleRel entity = userRoleRelService.getById(id);
        return R.success(BeanUtil.toBean(entity, UserRoleRelVo.class));
    }

    /**
     * 分页查询用户角色关联。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询用户角色关联")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<UserRoleRelVo>> page(@RequestBody @Validated PageParams<UserRoleRelQuery> params) {
        Page<UserRoleRelVo> page = Page.of(params.getCurrent(), params.getSize());
        UserRoleRel entity = BeanUtil.toBean(params.getModel(), UserRoleRel.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        userRoleRelService.pageAs(page, wrapper, UserRoleRelVo.class);
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
    public R<List<UserRoleRelVo>> list(@RequestBody @Validated UserRoleRelQuery params) {
        UserRoleRel entity = BeanUtil.toBean(params, UserRoleRel.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<UserRoleRelVo> listVo = userRoleRelService.listAs(wrapper, UserRoleRelVo.class);
        return R.success(listVo);
    }
}
