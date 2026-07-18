package top.mddata.console.controller.message;

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
import top.mddata.console.dto.message.MsgSendDto;
import top.mddata.console.dto.message.MsgTaskDto;
import top.mddata.console.entity.message.MsgTask;
import top.mddata.console.mapper.message.MsgTaskMapper;
import top.mddata.console.query.message.MsgTaskQuery;
import top.mddata.console.service.message.MsgTaskRecipientService;
import top.mddata.console.service.message.MsgTaskService;
import top.mddata.console.vo.message.MsgTaskVo;

import java.util.List;

/**
 * 消息任务 控制层。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@RestController
@Validated
@Tag(name = "消息任务")
@RequestMapping("/message/msgTask")
@RequiredArgsConstructor
public class MsgTaskController extends SuperController<MsgTaskService, MsgTask> {
    private final MsgTaskRecipientService msgTaskRecipientService;
    private final MsgTaskMapper msgTaskMapper;

    @PostMapping("/listByTitle")
    @RequestLog(value = "根据标题查询消息任务", logType = RequestLog.LogType.QUERY)
    public Object listByTitle(String title) {
        return R.success(msgTaskMapper.listByTitle(title));
    }

    /**
     * 添加消息任务。
     *
     * @param dto 消息任务
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Operation(summary = "新增", description = "保存消息任务")
    @RequestLog(value = "新增", logType = RequestLog.LogType.ADD, request = false)
    public R<Long> save(@Validated @RequestBody MsgTaskDto dto) {
        return R.success(superService.saveDto(dto).getId());
    }

    /**
     * 根据主键删除消息任务。
     *
     * @param ids 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/delete")
    @Operation(summary = "删除", description = "根据主键删除消息任务")
    @RequestLog(value = "'删除:' + #ids", logType = RequestLog.LogType.DELETE)
    public R<Boolean> delete(@RequestBody List<Long> ids) {
        return R.success(superService.removeByIds(ids));
    }

    /**
     * 根据主键更新消息任务。
     *
     * @param dto 消息任务
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PostMapping("/update")
    @Operation(summary = "修改", description = "根据主键更新消息任务")
    @RequestLog(value = "修改", logType = RequestLog.LogType.UPDATE, request = false)
    public R<Long> update(@Validated(BaseEntity.Update.class) @RequestBody MsgTaskDto dto) {
        return R.success(superService.updateDtoById(dto).getId());
    }

    /**
     * 根据消息任务主键获取详细信息。
     *
     * @param id 消息任务主键
     * @return 消息任务详情
     */
    @GetMapping("/getById")
    @Operation(summary = "单体查询", description = "根据主键获取消息任务")
    @RequestLog(value = "'单体查询:' + #id", logType = RequestLog.LogType.QUERY)
    public R<MsgTaskVo> get(@RequestParam Long id) {
        MsgTask entity = superService.getById(id);
        MsgTaskVo vo = BeanUtil.toBean(entity, MsgTaskVo.class);
//        if (vo != null) {
//            List<MsgTaskRecipient> msgTaskRecipientList = msgTaskRecipientService.listByMsgTaskId(vo.getId());
//            vo.setRecipientList(msgTaskRecipientList.stream().map(MsgTaskRecipient::getRecipient).toList());
//        }
        return R.success(vo);
    }

    /**
     * 分页查询消息任务。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询消息任务")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<MsgTaskVo>> page(@RequestBody @Validated PageParams<MsgTaskQuery> params) {
        Page<MsgTaskVo> page = Page.of(params.getCurrent(), params.getSize());
        MsgTask entity = BeanUtil.toBean(params.getModel(), MsgTask.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        wrapper.in(MsgTask::getChannel, params.getModel().getChannelList());
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, MsgTaskVo.class);
        return R.success(page);
    }


    @Operation(summary = "发布站内信", description = "发布站内信")
    @PostMapping("/publish")
    @RequestLog(value = "发布站内信", logType = RequestLog.LogType.ADD)
    public R<Boolean> publish(@RequestBody @Validated MsgTaskDto data) {
        return R.success(superService.publish(data));
    }

    /**
     * 根据消息模板发送消息
     *
     * @param data 消息参数
     */
    @PostMapping("/sendByTemplateKey")
    @RequestLog(value = "根据模板发送消息", logType = RequestLog.LogType.ADD)
    public R<Long> sendByTemplateKey(@RequestBody @Validated MsgSendDto data) {
        return R.success(superService.sendByTemplateKey(data));
    }
}
