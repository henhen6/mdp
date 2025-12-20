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
import top.mddata.console.message.dto.NoticeRecipientDto;
import top.mddata.console.message.entity.NoticeRecipient;
import top.mddata.console.message.query.NoticeRecipientQuery;
import top.mddata.console.message.service.NoticeRecipientService;
import top.mddata.console.message.vo.NoticeRecipientVo;

import java.util.List;

/**
 * 通知接收人 控制层。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@RestController
@Validated
@Tag(name = "通知接收人")
@RequestMapping("/message/noticeRecipient")
@RequiredArgsConstructor
public class NoticeRecipientController extends SuperController<NoticeRecipientService, NoticeRecipient> {
    /**
     * 添加通知接收人。
     *
     * @param dto 通知接收人
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存通知接收人")
    @RequestLog(value = "新增", request = false)
    public R<Long> save(@Validated @RequestBody NoticeRecipientDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除通知接收人。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除通知接收人")
    @RequestLog("'删除:' + #ids")
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新通知接收人。
     *
     * @param dto 通知接收人
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新通知接收人")
    @RequestLog(value = "修改", request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody NoticeRecipientDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 根据通知接收人主键获取详细信息。
     *
     * @param id 通知接收人主键
     * @return 通知接收人详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取通知接收人")
    @RequestLog("'单体查询:' + #id")
    public R<NoticeRecipientVo> get(@RequestParam Long id) {
        NoticeRecipient entity = superService.getById(id);
        return R.success(BeanUtil.toBean(entity, NoticeRecipientVo.class));
    }

    /**
     * 分页查询通知接收人。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询通知接收人")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", response = false)
    public R<Page<NoticeRecipientVo>> page(@RequestBody @Validated PageParams<NoticeRecipientQuery> params) {
        Page<NoticeRecipientVo> page = Page.of(params.getCurrent(), params.getSize());
        NoticeRecipient entity = BeanUtil.toBean(params.getModel(), NoticeRecipient.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, NoticeRecipientVo.class);
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
    public R<List<NoticeRecipientVo>> list(@RequestBody @Validated NoticeRecipientQuery params) {
        NoticeRecipient entity = BeanUtil.toBean(params, NoticeRecipient.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        List<NoticeRecipientVo> listVo = superService.listAs(wrapper, NoticeRecipientVo.class);
        return R.success(listVo);
    }
}
