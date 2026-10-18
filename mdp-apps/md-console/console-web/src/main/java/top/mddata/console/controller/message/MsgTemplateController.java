package top.mddata.console.controller.message;

import java.util.List;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.R;
import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.console.dto.message.MsgTemplateDto;
import top.mddata.console.entity.message.MsgTemplate;
import top.mddata.console.query.message.MsgTemplateQuery;
import top.mddata.console.service.message.MsgTemplateService;
import top.mddata.console.vo.message.MsgTemplateVo;

/**
 * 消息模板 控制层。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@RestController
@Validated
@Tag(name = "消息模板")
@RequestMapping("/message/msgTemplate")
@RequiredArgsConstructor
public class MsgTemplateController extends SuperController<MsgTemplateService, MsgTemplate> {
    /**
     * 添加消息模板。
     *
     * @param dto 消息模板
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存消息模板")
    @RequestLog(value = "新增", logType = RequestLog.LogType.ADD, request = false)
    public R<Long> save(@Validated @RequestBody MsgTemplateDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除消息模板。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除消息模板")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新消息模板。
     *
     * @param dto 消息模板
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新消息模板")
    @RequestLog(value = "修改", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody MsgTemplateDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 分页查询消息模板。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询消息模板")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<MsgTemplateVo>> page(@RequestBody @Validated PageParams<MsgTemplateQuery> params) {
        Page<MsgTemplateVo> page = Page.of(params.getCurrent(), params.getSize());
        MsgTemplate entity = BeanUtil.toBean(params.getModel(), MsgTemplate.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, MsgTemplateVo.class);
        return R.success(page);
    }

}
