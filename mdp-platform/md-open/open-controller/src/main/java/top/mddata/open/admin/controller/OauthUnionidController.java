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
import top.mddata.open.admin.dto.OauthUnionidDto;
import top.mddata.open.admin.entity.OauthUnionid;
import top.mddata.open.admin.query.OauthUnionidQuery;
import top.mddata.open.admin.service.OauthUnionidService;
import top.mddata.open.admin.vo.OauthUnionidVo;

import java.util.List;

/**
 * unionid 控制层。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@RestController
@Validated
@Tag(name = "unionid")
@RequestMapping("/admin/oauthUnionid")
@RequiredArgsConstructor
public class OauthUnionidController extends SuperController<OauthUnionidService, OauthUnionid> {
    /**
     * 添加unionid。
     *
     * @param dto unionid
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存unionid")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody OauthUnionidDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除unionid。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除unionid")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新unionid。
     *
     * @param dto unionid
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新unionid")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody OauthUnionidDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 根据unionid主键获取详细信息。
     *
     * @param id unionid主键
     * @return unionid详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取unionid")
    @RequestLog("'单体查询:' + #id")
    public R<OauthUnionidVo> get(@RequestParam Long id) {
        OauthUnionid entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, OauthUnionidVo.class));
    }

    /**
     * 分页查询unionid。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询unionid")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<OauthUnionidVo>> page(@RequestBody @Validated PageParams<OauthUnionidQuery> params) {
        Page<OauthUnionidVo> page = Page.of(params.getCurrent(), params.getSize());
        OauthUnionid entity = BeanUtil.toBean(params.getModel(), OauthUnionid.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, OauthUnionidVo.class);
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
    public R<List<OauthUnionidVo>> list(@RequestBody @Validated OauthUnionidQuery params) {
        OauthUnionid entity = BeanUtil.toBean(params, OauthUnionid.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<OauthUnionidVo> listVo = superService.listAs(wrapper, OauthUnionidVo.class);
        return R.success(listVo);
    }


    @GetMapping("/getBySubjectIdAndUserId")
    @Operation(summary = "根据主体id和用户id单体查询", description = "根据主体id和用户id单体查询")
    @RequestLog("'根据主体id和用户id单体查询:' + #subjectId + '---' + #userId")
    public R<OauthUnionidVo> getBySubjectIdAndUserId(@RequestParam Long subjectId, @RequestParam Long userId) {
        return R.success(superService.getBySubjectIdAndUserId(subjectId, userId));
    }
}
