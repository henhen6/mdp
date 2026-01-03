package top.mddata.open.client;

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
import top.mddata.base.utils.ContextUtil;
import top.mddata.open.admin.entity.App;
import top.mddata.open.admin.entity.AppKeys;
import top.mddata.open.admin.query.AppQuery;
import top.mddata.open.admin.service.AppKeysService;
import top.mddata.open.admin.service.AppService;
import top.mddata.open.admin.utils.RsaTool;
import top.mddata.open.admin.vo.AppKeysVo;
import top.mddata.open.admin.vo.AppVo;
import top.mddata.open.client.dto.AppDevInfoDto;
import top.mddata.open.client.dto.AppEventSubscriptionDto;
import top.mddata.open.client.dto.AppInfoUpdateDto;
import top.mddata.open.client.dto.AppKeysUpdateDto;

import java.util.List;

/**
 * 开发者申请通过后的应用 控制层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@RestController
@Validated
@Tag(name = "我的应用")
@RequestMapping("/client/app")
@RequiredArgsConstructor
public class ClientAppController extends SuperController<AppService, App> {
    private final AppKeysService appKeysService;

    /**
     * 根据主键更新应用。
     *
     * @param dto 应用
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改应用信息", description = "根据主键更新应用信息")
    @RequestLog(value = "修改应用信息")
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody AppInfoUpdateDto dto) {
        return R.success(superService.updateInfoById(dto));
    }

    /**
     * 根据主键更新应用。
     *
     * @param dto 应用
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/updateDev")
    @Operation(summary = "修改应用开发信息", description = "根据主键更新应用开发信息")
    @RequestLog(value = "修改应用开发信息")
    public R<Long> updateDev(@Validated(BaseEntity.Update.class) @RequestBody AppDevInfoDto dto) {
        return R.success(superService.updateDevById(dto));
    }

    /**
     * 修改秘钥
     *
     * @param param 表单数据
     * @return 返回影响行数
     */
    @PostMapping("/updateKeys")
    @Operation(summary = "修改应用秘钥信息", description = "修改应用秘钥信息")
    @RequestLog(value = "修改应用秘钥信息")
    public R<AppKeys> updateKeys(@Validated @RequestBody AppKeysUpdateDto param) {
        return R.success(appKeysService.updateKeysByClient(param));
    }

    /**
     * 修改事件订阅
     *
     * @param param 表单数据
     * @return 返回影响行数
     */
    @PostMapping("/updateEventSubscription")
    @Operation(summary = "修改事件订阅", description = "修改事件订阅")
    @RequestLog(value = "修改事件订阅")
    public R<Long> updateEventSubscription(@Validated @RequestBody AppEventSubscriptionDto param) {
        return R.success(appKeysService.updateEventSubscription(param));
    }

    /**
     * 根据主键删除应用。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除应用")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据应用主键获取详细信息。
     *
     * @param id 应用主键
     * @return 应用详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取应用")
    @RequestLog("'单体查询:' + #id")
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
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<AppVo>> page(@RequestBody @Validated PageParams<AppQuery> params) {
        Page<AppVo> page = Page.of(params.getCurrent(), params.getSize());
        App entity = BeanUtil.toBean(params.getModel(), App.class);
        entity.setCreatedBy(ContextUtil.getUserId());
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, AppVo.class);
        return R.success(page);
    }


    /**
     * 获取秘钥信息
     *
     * @param appId 应用ID
     * @return 秘钥
     */
    @GetMapping("getKeys")
    @Operation(summary = "获取秘钥信息", description = "获取秘钥信息")
    @RequestLog(value = "获取秘钥信息")
    public R<AppKeysVo> getKeys(@RequestParam Long appId) {
        return R.success(superService.getKeys(appId, false));
    }

    /**
     * 重置开发者秘钥
     *
     * @param appId 应用ID
     * @param keyFormat     秘钥格式，1：PKCS8(JAVA适用)，2：PKCS1(非JAVA适用)
     * @return 秘钥
     * @throws Exception 异常
     */
    @PostMapping("resetAppKeys")
    @Operation(summary = "重置秘钥", description = "重置秘钥")
    @RequestLog(value = "重置秘钥")
    public R<RsaTool.KeyStore> resetAppKeys(@RequestParam Long appId, @RequestParam Integer keyFormat) throws Exception {
        return R.success(superService.resetAppKeys(appId, keyFormat));
    }

}
