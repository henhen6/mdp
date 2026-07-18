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
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.base.util.ContextUtil;
import top.mddata.open.dto.admin.AppDto;
import top.mddata.open.dto.admin.AppGroupRelDto;
import top.mddata.open.dto.admin.AppKeysDto;
import top.mddata.open.entity.admin.App;
import top.mddata.open.entity.admin.AppGroupRel;
import top.mddata.open.entity.admin.AppKeys;
import top.mddata.open.query.admin.AppQuery;
import top.mddata.open.service.admin.AppGroupRelService;
import top.mddata.open.service.admin.AppKeysService;
import top.mddata.open.service.admin.AppService;
import top.mddata.open.vo.admin.AppKeysVo;
import top.mddata.open.vo.admin.AppVo;

import java.util.List;

/**
 * 应用 控制层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@RestController
@Validated
@Tag(name = "应用")
@RequestMapping("/admin/app")
@RequiredArgsConstructor
public class AppController extends SuperController<AppService, App> {
    private final AppKeysService appKeysService;
    private final AppGroupRelService appGroupRelService;

    /**
     * 添加应用。
     *
     * @param dto 应用
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存应用")
    @RequestLog(value = "新增", logType = RequestLog.LogType.ADD, request = false)
    public R<Long> save(@Validated @RequestBody AppDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除应用。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除应用")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新应用。
     *
     * @param dto 应用
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新应用")
    @RequestLog(value = "修改", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody AppDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 根据应用主键获取详细信息。
     *
     * @param id 应用主键
     * @return 应用详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取应用")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<AppVo> get(@RequestParam Long id) {
        App entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, AppVo.class));
    }

    /**
     * 分页查询应用。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询应用")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<AppVo>> page(@RequestBody @Validated PageParams<AppQuery> params) {
        Page<AppVo> page = Page.of(params.getCurrent(), params.getSize());
        App entity = BeanUtil.toBean(params.getModel(), App.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, AppVo.class);
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
    public R<List<AppVo>> list(@RequestBody @Validated AppQuery params) {
        App entity = BeanUtil.toBean(params, App.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<AppVo> listVo = superService.listAs(wrapper, AppVo.class);
        return R.success(listVo);
    }

    /**
     * 根据应用id获取详细信息。
     *
     * @param appKey 应用标识
     * @return 应用详情
     */
    @GetMapping("/getAppByAppKey")
    @Operation(summary = "单体查询-appKey", description = "根据应用id获取应用")
    @RequestLog(value = "'单体查询-appKey:' + #appKey", logType = RequestLog.LogType.QUERY)
    public R<AppVo> getAppByAppKey(@RequestParam String appKey) {
        return R.success(superService.getAppByAppKey(appKey));
    }

    /**
     * 查询需要 接收事件推送的应用
     *
     * @return 应用
     */
    @GetMapping("/listNeedPushApp")
    @Operation(summary = "查询需要接收事件推送的应用", description = "查询需要接收事件推送的应用")
    @RequestLog(value = "查询需要接收事件推送的应用", logType = RequestLog.LogType.QUERY)
    public R<List<AppVo>> listNeedPushApp() {
        return R.success(superService.listNeedPushApp());
    }


    /**
     * 查询用户能访问的应用
     *
     */
    @PostMapping("listMyApp")
    @Operation(summary = "查询用户能访问的应用", description = "查询用户能访问的应用")
    @RequestLog(value = "查询用户能访问的应用", logType = RequestLog.LogType.QUERY)
    public R<List<AppVo>> listMyApp() {
        List<AppVo> list = superService.listMyApp(ContextUtil.getUserId());
        return R.success(list);
    }

    /**
     * 校验用户是否拥有该应用
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return 是否拥有该应用
     */
    @GetMapping("/checkAppByUserId")
    @Operation(summary = "校验用户是否拥有该应用", description = "校验用户是否拥有该应用")
    @RequestLog(value = "校验用户是否拥有该应用", logType = RequestLog.LogType.QUERY)
    public R<Boolean> checkAppByUserId(@RequestParam Long userId, @RequestParam Long appId) {
        return R.success(superService.checkAppByUserId(userId, appId));
    }


    /**
     * 获取秘钥信息
     *
     * @param appId 应用ID
     * @return 秘钥
     */
    @GetMapping("getKeys")
    @Operation(summary = "获取秘钥信息", description = "获取秘钥信息")
    @RequestLog(value = "获取秘钥信息", logType = RequestLog.LogType.QUERY)
    public R<AppKeysVo> getKeys(@RequestParam Long appId) {
        return R.success(appKeysService.getKeys(appId));
    }

    /**
     * 根据appKey查询应用秘钥（含通知加密配置）
     * 供内部Feign调用（如 api-web 的回调控制器）
     *
     * @param appKey 应用标识
     * @return 秘钥VO
     */
    @GetMapping("/getAppKeysByAppKey")
    @Operation(summary = "根据appKey查询秘钥", description = "根据appKey查询应用秘钥（含通知加密配置）")
    @RequestLog(value = "'根据appKey查询秘钥:' + #appKey", logType = RequestLog.LogType.QUERY)
    public R<AppKeysVo> getAppKeysByAppKey(@RequestParam String appKey) {
        return R.success(appKeysService.getByAppKey(appKey));
    }

    /**
     * 修改应用秘钥配置（通知地址、加密模式、Token、EncodingAESKey等）
     *
     * @param param 表单数据
     * @return 返回影响行数
     */
    @PostMapping("/updateKeys")
    @Operation(summary = "修改秘钥配置", description = "修改应用秘钥配置")
    @RequestLog(value = "修改秘钥配置", logType = RequestLog.LogType.UPDATE)
    public R<AppKeys> updateKeys(@Validated @RequestBody AppKeysDto param) {
        return R.success(appKeysService.saveDto(param));
    }


    @Operation(summary = "查询拥有的权限组", description = "根据应用ID查询拥有的权限组")
    @RequestLog(value = "查询拥有的权限组", logType = RequestLog.LogType.QUERY)
    @GetMapping("/listGroupIdByAppId")
    public R<List<Long>> listGroupIdByAppId(@RequestParam Long appId) {
        return R.success(appGroupRelService.listGroupIdByAppId(appId));
    }

    /**
     * 给应用授权权限组
     *
     * @param param 表单数据
     * @return 返回影响行数
     */
    @Operation(summary = "给应用授权权限组", description = "设置应用的权限组")
    @RequestLog(value = "给应用授权权限组", logType = RequestLog.LogType.ADD)
    @PostMapping("/saveAppGroup")
    public R<AppGroupRel> saveAppGroup(@Validated @RequestBody AppGroupRelDto param) {
        return R.success(appGroupRelService.saveDto(param));
    }


    /**
     * 根据角色模板ID，分页查询角色模板拥有的应用 或者 没有的应用
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/pageByRoleTemplateId")
    @Operation(summary = "根据角色模板ID分页查询应用", description = "根据角色模板ID分页查询应用")
    @RequestLog(value = "'根据角色模板ID分页查询应用:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<AppVo>> pageByRoleTemplateId(@RequestBody @Validated(AppQuery.RolePage.class) PageParams<AppQuery> params) {
        Page<App> page = Page.of(params.getCurrent(), params.getSize());
        return R.success(superService.pageByRoleTemplateId(page, params.getModel()));
    }

    /**
     * 根据权限角色ID 分页查询角色拥有的应用 或者 没有的应用
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/pageByRoleId")
    @Operation(summary = "根据角色ID分页查询应用", description = "根据角色ID分页查询应用")
    @RequestLog(value = "'根据角色ID分页查询应用:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<AppVo>> pageByRoleId(@RequestBody @Validated(AppQuery.RolePage.class) PageParams<AppQuery> params) {
        Page<App> page = Page.of(params.getCurrent(), params.getSize());
        return R.success(superService.pageByRoleId(page, params.getModel()));
    }
}
