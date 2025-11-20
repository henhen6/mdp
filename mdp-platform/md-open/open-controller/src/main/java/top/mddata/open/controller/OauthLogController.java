package top.mddata.open.controller;

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
import top.mddata.open.dto.OauthLogDto;
import top.mddata.open.entity.OauthLog;
import top.mddata.open.query.OauthLogQuery;
import top.mddata.open.service.OauthLogService;
import top.mddata.open.vo.OauthLogVo;

import java.util.List;

/**
 * 授权记录 控制层。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@RestController
@Validated
@Tag(name = "授权记录")
@RequestMapping("//oauthLog")
@RequiredArgsConstructor
public class OauthLogController extends SuperController<OauthLogService, OauthLog> {
    /**
     * 添加授权记录。
     *
     * @param dto 授权记录
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存授权记录")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody OauthLogDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除授权记录。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除授权记录")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新授权记录。
     *
     * @param dto 授权记录
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新授权记录")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody OauthLogDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 根据授权记录主键获取详细信息。
     *
     * @param id 授权记录主键
     * @return 授权记录详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取授权记录")
    @RequestLog("'单体查询:' + #id")
    public R<OauthLogVo> get(@RequestParam Long id) {
        OauthLog entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, OauthLogVo.class));
    }

    /**
     * 分页查询授权记录。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询授权记录")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<OauthLogVo>> page(@RequestBody @Validated PageParams<OauthLogQuery> params) {
        Page<OauthLogVo> page = Page.of(params.getCurrent(), params.getSize());
        OauthLog entity = BeanUtil.toBean(params.getModel(), OauthLog.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, OauthLogVo.class);
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
    public R<List<OauthLogVo>> list(@RequestBody @Validated OauthLogQuery params) {
        OauthLog entity = BeanUtil.toBean(params, OauthLog.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<OauthLogVo> listVo = superService.listAs(wrapper, OauthLogVo.class);
        return R.success(listVo);
    }
}
