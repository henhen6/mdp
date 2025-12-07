package top.mddata.console.organization.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryMethods;
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
import top.mddata.base.interfaces.echo.EchoService;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.common.entity.User;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.console.organization.dto.UserDto;
import top.mddata.console.organization.dto.UserResetPasswordDto;
import top.mddata.console.organization.dto.UserUpdateDto;
import top.mddata.console.organization.query.UserQuery;
import top.mddata.console.organization.service.UserService;
import top.mddata.console.organization.vo.UserVo;

import java.util.List;

/**
 * 用户 控制层。
 *
 * @author henhen6
 * @since 2025-11-12 19:50:17
 */
@RestController
@Validated
@Tag(name = "用户")
@RequestMapping("/organization/user")
@RequiredArgsConstructor
public class UserController extends SuperController<UserService, User> {
    private final EchoService echoService;

    /**
     * 添加用户。
     *
     * @param dto 用户
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存用户")
    @RequestLog(value = "新增")
    public R<Long> save(@Validated @RequestBody UserDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除用户。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除用户")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新用户。
     *
     * @param dto 用户
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新用户")
    @RequestLog(value = "修改")
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody UserUpdateDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 根据用户主键获取详细信息。
     *
     * @param id 用户主键
     * @return 用户详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取用户")
    @RequestLog("'单体查询:' + #id")
    public R<UserVo> get(@RequestParam Long id) {
        User entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, UserVo.class));
    }

    /**
     * 分页查询用户。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询用户")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'")
    public R<Page<UserVo>> page(@RequestBody @Validated PageParams<UserQuery> params) {
        Page<UserVo> page = superService.page(params);
        echoService.action(page);
        return R.success(page);
    }

    /**
     * 批量查询
     * @param params 查询参数
     * @return 集合
     */
    @PostMapping("/list")
    @Operation(summary = "批量查询", description = "批量查询")
    @RequestLog(value = "批量查询")
    public R<List<UserVo>> list(@RequestBody @Validated UserQuery params) {
        User entity = BeanUtil.toBean(params, User.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<UserVo> listVo = superService.listAs(wrapper, UserVo.class);
        return R.success(listVo);
    }

    /**
     * 重置密码
     *
     * @param data 修改实体
     * @return 是否成功
     */
    @Operation(summary = "重置密码", description = "重置密码")
    @PostMapping("/resetPassword")
    @RequestLog("重置密码")
    public R<Boolean> resetPassword(@RequestBody @Validated UserResetPasswordDto data) {
        return R.success(superService.resetPassword(data));
    }

    /**
     * 账号解锁。
     *
     * @param id 用户id
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/unlock")
    @Operation(summary = "账号解锁", description = "根据主键账号解锁")
    @RequestLog(value = "账号解锁")
    public R<Boolean> unlock(@RequestParam Long id) {
        return R.success(superService.unlock(id));
    }


    @GetMapping("/checkUsername")
    @Operation(summary = "检测用户名是否存在", description = "检测用户名是否存在")
    @RequestLog(value = "检测用户名是否存在")
    public R<Boolean> checkUsername(@RequestParam String username, @RequestParam(required = false) Long id) {
        return R.success(superService.checkUsername(username, id));
    }

    @GetMapping("/checkEmail")
    @Operation(summary = "检测邮箱是否存在", description = "检测邮箱是否存在")
    @RequestLog(value = "检测邮箱是否存在")
    public R<Boolean> checkEmail(@RequestParam String email, @RequestParam(required = false) Long id) {
        return R.success(superService.checkEmail(email, id));
    }

    @GetMapping("/checkPhone")
    @Operation(summary = "检测手机号是否存在", description = "检测手机号是否存在")
    @RequestLog(value = "检测手机号是否存在")
    public R<Boolean> checkPhone(@RequestParam String phone, @RequestParam(required = false) Long id) {
        return R.success(superService.checkPhone(phone, id));
    }

    /**
     * 根据权限角色ID 分页查询角色拥有的用户 或者 没有的用户
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/pageByRoleId")
    @Operation(summary = "根据角色ID分页查询用户", description = "根据角色ID分页查询用户")
    @RequestLog(value = "'根据角色ID分页查询用户:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<UserVo>> pageByRoleId(@RequestBody @Validated(UserQuery.RolePage.class) PageParams<UserQuery> params) {

        Page<UserVo> page = Page.of(params.getCurrent(), params.getSize());
        User entity = BeanUtil.toBean(params.getModel(), User.class);
        UserQuery query = params.getModel();
        QueryWrapper wrapper;
        if (query.getHasUser() == null || !query.getHasUser()) {
            // 相当于 not exists 查询
            /*
            wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()))
                    .leftJoin(UserRoleRel.class)
                    .on(wrap -> wrap.where(User::getId).eq(UserRoleRel::getUserId).and(UserRoleRel::getRoleId).eq(query.getRoleId()))
                    .isNull(UserRoleRel::getUserId);
            */
            wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
            QueryWrapper userRoleWrapper = QueryWrapper.create().select("1").from(UserRoleRel.class).where(UserRoleRel::getUserId).eq(User::getId).eq(UserRoleRel::getRoleId, query.getRoleId());
            wrapper.where(QueryMethods.notExists(userRoleWrapper));
        } else {
            // 相当于 exists 查询
            /*
            wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()))
                    .innerJoin(UserRoleRel.class)
                    .on(UserRoleRel::getUserId, User::getId)
                    .eq(UserRoleRel::getRoleId, query.getRoleId());
             */
            /*
            select * from mdc_user u
            where EXISTS ( select 1 from mdc_user_role_rel ur where ur.user_id = u.id and ur.role_id = 1)
            */
            wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
            QueryWrapper userRoleWrapper = QueryWrapper.create().select("1").from(UserRoleRel.class).where(UserRoleRel::getUserId).eq(User::getId).eq(UserRoleRel::getRoleId, query.getRoleId());
            wrapper.where(QueryMethods.exists(userRoleWrapper));
        }


        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, UserVo.class);
        return R.success(page);

    }
}
