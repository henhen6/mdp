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
import top.mddata.console.dto.system.RequestLogDto;
import top.mddata.console.service.system.RequestLogService;

/**
 * 内部-请求日志入口：仅供平台内部服务（各服务 WebLog 拦截器）经 Feign 调用。
 * <p>
 * 注意：本端点不可再加 @RequestLog 等业务日志注解，
 * 否则"记录日志"这一行为本身会再产生日志，造成递归记录。
 * </p>
 */
@RestController
@Validated
@Tag(name = "内部-请求日志")
@RequestMapping("/inner/system/requestLog")
@RequiredArgsConstructor
public class InnerRequestLogController {

    private final RequestLogService requestLogService;

    /**
     * 添加请求日志
     *
     * @param dto 请求日志
     */
    @Operation(hidden = true)
    @PostMapping("/save")
    public R<Long> save(@Validated @RequestBody RequestLogDto dto) {
        return R.success(requestLogService.saveDto(dto).getId());
    }
}
