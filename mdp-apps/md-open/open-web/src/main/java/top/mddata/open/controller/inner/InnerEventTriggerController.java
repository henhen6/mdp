package top.mddata.open.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.open.dto.admin.EventTriggerDto;
import top.mddata.open.entity.admin.EventTrigger;
import top.mddata.open.service.notify.event.EventTriggerService;

/**
 * 内部-事件触发入口：仅供平台内部服务（console、workbench 等）经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-事件触发")
@RequestMapping("/inner/eventTrigger")
@RequiredArgsConstructor
public class InnerEventTriggerController {

    private final EventTriggerService eventTriggerService;

    @Operation(hidden = true)
    @PostMapping("/save")
    public R<EventTrigger> save(@Validated @RequestBody EventTriggerDto save) {
        return R.success(eventTriggerService.save(save));
    }
}
