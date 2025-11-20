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
import top.mddata.open.admin.dto.OauthScopeDto;
import top.mddata.open.admin.entity.GroupScopeRel;
import top.mddata.open.admin.entity.OauthScope;
import top.mddata.open.admin.query.OauthScopeQuery;
import top.mddata.open.admin.service.OauthScopeService;
import top.mddata.open.admin.vo.OauthScopeVo;

import java.util.List;

/**
 * oauth2权限 控制层。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@RestController
@Validated
@Tag(name = "oauth2权限")
@RequestMapping("/admin/oauthScope")
@RequiredArgsConstructor
public class OauthScopeController extends SuperController<OauthScopeService, OauthScope> {
    /**
     * 添加oauth2权限。
     *
     * @param dto oauth2权限
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存oauth2权限")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody OauthScopeDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除oauth2权限。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除oauth2权限")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新oauth2权限。
     *
     * @param dto oauth2权限
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新oauth2权限")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody OauthScopeDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 根据oauth2权限主键获取详细信息。
     *
     * @param id oauth2权限主键
     * @return oauth2权限详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取oauth2权限")
    @RequestLog("'单体查询:' + #id")
    public R<OauthScopeVo> get(@RequestParam Long id) {
        OauthScope entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, OauthScopeVo.class));
    }

    /**
     * 分页查询oauth2权限。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询oauth2权限")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<OauthScopeVo>> page(@RequestBody @Validated PageParams<OauthScopeQuery> params) {
        Page<OauthScopeVo> page = Page.of(params.getCurrent(), params.getSize());
        OauthScope entity = BeanUtil.toBean(params.getModel(), OauthScope.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, OauthScopeVo.class);
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
    public R<List<OauthScopeVo>> list(@RequestBody @Validated OauthScopeQuery params) {
        OauthScope entity = BeanUtil.toBean(params, OauthScope.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<OauthScopeVo> listVo = superService.listAs(wrapper, OauthScopeVo.class);
        return R.success(listVo);
    }


    /**
     * 检测权限编码是否存在
     *
     * @param code 权限编码
     * @param id   权限id
     * @return true 存在， false 不存在
     */
    @GetMapping("/check")
    @Operation(summary = "检测权限编码是否存在", description = "检测权限编码是否存在")
    @RequestLog("检测权限编码是否存在")
    public R<Boolean> check(@RequestParam String code, @RequestParam(required = false) Long id) {
        return R.success(superService.check(code, id));
    }


    @Operation(summary = "根据权限编码查询应用权限", description = "根据权限编码查询应用权限")
    @GetMapping("/getScopeListByCode")
    public R<List<OauthScopeVo>> getScopeListByCode(List<String> scopes) {
        return R.success(superService.getScopeListByCode(scopes));
    }

    /**
     * 根据应用id查询应用拥有的权限
     *
     * @param appId 应用id
     * @return 权限
     */
    @Operation(summary = "根据应用id查询应用拥有的权限", description = "根据应用id查询应用拥有的权限")
    @GetMapping("/listByAppId")
    public R<List<OauthScopeVo>> listByAppId(@RequestParam Long appId) {
        return R.success(superService.listByAppId(appId));
    }


    /**
     * 根据权限分组ID分页查询权限
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/pageByGroupId")
    @Operation(summary = "根据应用权限分组ID分页查询权限", description = "根据应用权限分组ID分页查询权限")
    @RequestLog(value = "'根据应用权限分组ID分页查询权限:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<OauthScopeVo>> pageByGroupId(@RequestBody @Validated(OauthScopeQuery.GroupPage.class) PageParams<OauthScopeQuery> params) {
        Page<OauthScopeVo> page = Page.of(params.getCurrent(), params.getSize());
        OauthScope entity = BeanUtil.toBean(params.getModel(), OauthScope.class);
        OauthScopeQuery query = params.getModel();
        QueryWrapper wrapper;
        if (query.getHasAuth() == null || !query.getHasAuth()) {
            // 相当于 not exists 查询
            wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()))
                    .leftJoin(GroupScopeRel.class)
                    .on(wrap -> wrap.where(OauthScope::getId).eq(GroupScopeRel::getScopeId).and(GroupScopeRel::getGroupId).eq(query.getGroupId()))
                    .isNull(GroupScopeRel::getScopeId);
        } else {
            // 相当于 exists 查询
            wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()))
                    .innerJoin(GroupScopeRel.class)
                    .on(GroupScopeRel::getScopeId, OauthScope::getId)
                    .eq(GroupScopeRel::getGroupId, query.getGroupId());
        }

        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, OauthScopeVo.class);
        return R.success(page);
    }
}
