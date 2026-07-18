package top.mddata.console.controller.message;

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
import top.mddata.base.mvcflex.controller.SuperController;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.console.entity.message.MsgTaskRecipient;
import top.mddata.console.query.message.MsgTaskRecipientQuery;
import top.mddata.console.service.message.MsgTaskRecipientService;
import top.mddata.console.vo.message.MsgTaskRecipientVo;

/**
 * 任务接收人 控制层。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@RestController
@Validated
@Tag(name = "任务接收人")
@RequestMapping("/message/msgTaskRecipient")
@RequiredArgsConstructor
public class MsgTaskRecipientController extends SuperController<MsgTaskRecipientService, MsgTaskRecipient> {

    /**
     * 分页查询任务接收人。
     *
     * @param params 分页对象
     * @return 分页对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页列表查询", description = "分页查询任务接收人")
    @RequestLog(value = "'分页列表查询:第' + #params?.current + '页, 显示' + #params?.size + '行'", logType = RequestLog.LogType.QUERY, response = false)
    public R<Page<MsgTaskRecipientVo>> page(@RequestBody @Validated PageParams<MsgTaskRecipientQuery> params) {
        Page<MsgTaskRecipientVo> page = Page.of(params.getCurrent(), params.getSize());
        MsgTaskRecipient entity = BeanUtil.toBean(params.getModel(), MsgTaskRecipient.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        superService.pageAs(page, wrapper, MsgTaskRecipientVo.class);
        return R.success(page);
    }

}
