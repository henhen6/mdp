package top.mddata.open.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.open.dto.admin.NotifyInfoDto;
import top.mddata.open.service.notify.callback.NotifyInfoService;

/**
 * 内部-接口回调入口：仅供平台内部服务（md-api 等）经 Feign 调用。
 */
@Slf4j
@RestController
@Validated
@Tag(name = "内部-接口回调")
@RequestMapping("/inner/notifyInfo")
@RequiredArgsConstructor
public class InnerNotifyController {

    private final NotifyInfoService notifyInfoService;

    /**
     * Feign 云路径边界：回调任务创建失败不能让调用方拿到 HTTP 500，
     * Service 层保持 fail fast 抛异常，这里 catch-all 降级为 R.fail。
     */
    @Operation(hidden = true)
    @PostMapping("/notify")
    public R<Long> notify(@Validated @RequestBody NotifyInfoDto request) {
        try {
            return notifyInfoService.notify(request);
        } catch (Exception e) {
            log.error("回调任务创建失败：{}", e.getMessage(), e);
            return R.fail("回调任务创建失败：" + e.getMessage());
        }
    }
}
