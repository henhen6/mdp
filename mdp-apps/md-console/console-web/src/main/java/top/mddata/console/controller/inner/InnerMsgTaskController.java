package top.mddata.console.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.console.dto.message.MsgSendDto;
import top.mddata.console.service.message.MsgTaskService;

/**
 * 内部-消息任务入口：仅供平台内部服务（md-api 消息开放接口等）经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-消息任务")
@RequestMapping("/inner/message/msgTask")
@RequiredArgsConstructor
public class InnerMsgTaskController {

    private final MsgTaskService msgTaskService;

    /**
     * 根据消息模板发送消息
     *
     * @param data 消息参数
     */
    @Operation(hidden = true)
    @PostMapping("/sendByTemplateKey")
    public R<Long> sendByTemplateKey(@RequestBody @Validated MsgSendDto data) {
        return R.success(msgTaskService.sendByTemplateKey(data));
    }
}
