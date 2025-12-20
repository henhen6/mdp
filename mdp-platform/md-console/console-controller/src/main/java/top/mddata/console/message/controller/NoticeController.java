package top.mddata.console.message.controller;

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
import top.mddata.console.message.dto.NoticeDto;
import top.mddata.console.message.entity.Notice;
import top.mddata.console.message.query.NoticeQuery;
import top.mddata.console.message.service.NoticeService;
import top.mddata.console.message.vo.NoticeVo;

import java.util.List;

/**
 * 站内通知 控制层。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@RestController
@Validated
@Tag(name = "站内通知")
@RequestMapping("/message/notice")
@RequiredArgsConstructor
public class NoticeController extends SuperController<NoticeService, Notice> {
    /**
     * 添加站内通知。
     *
     * @param dto 站内通知
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存站内通知")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody NoticeDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除站内通知。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除站内通知")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新站内通知。
     *
     * @param dto 站内通知
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新站内通知")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody NoticeDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 根据站内通知主键获取详细信息。
     *
     * @param id 站内通知主键
     * @return 站内通知详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取站内通知")
    @RequestLog("'单体查询:' + #id")
    public R<NoticeVo> get(@RequestParam Long id) {
        Notice entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, NoticeVo.class));
    }

    /**
     * 分页查询站内通知。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询站内通知")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<NoticeVo>> page(@RequestBody @Validated PageParams<NoticeQuery> params) {
        Page<NoticeVo> page = Page.of(params.getCurrent(), params.getSize());
        Notice entity = BeanUtil.toBean(params.getModel(), Notice.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, NoticeVo.class);
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
    public R<List<NoticeVo>> list(@RequestBody @Validated NoticeQuery params) {
        Notice entity = BeanUtil.toBean(params, Notice.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<NoticeVo> listVo = superService.listAs(wrapper, NoticeVo.class);
        return R.success(listVo);
    }
}
