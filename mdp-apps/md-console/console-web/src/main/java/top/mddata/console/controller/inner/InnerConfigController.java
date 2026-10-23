package top.mddata.console.controller.inner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.console.service.system.ConfigService;
import top.mddata.console.vo.system.ConfigVo;

/**
 * 内部-系统参数入口：仅供平台内部服务经 Feign 调用。
 */
@RestController
@Validated
@Tag(name = "内部-系统参数")
@RequestMapping("/inner/system/config")
@RequiredArgsConstructor
public class InnerConfigController {

    private final ConfigService configService;

    /**
     * 根据参数标识，查询系统参数VO
     * @param uniqKey 参数标识
     * @return 系统参数
     */
    @Operation(hidden = true)
    @GetMapping("/getConfig")
    public R<ConfigVo> getConfig(@RequestParam String uniqKey) {
        return R.success(configService.getConfig(uniqKey));
    }

    /**
     * 根据参数标识，查询长整型系统参数
     * @param uniqKey 参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    @Operation(hidden = true)
    @GetMapping("/getLong")
    public R<Long> getLong(@RequestParam String uniqKey, @RequestParam(required = false) Long defaultValue) {
        return R.success(configService.getLong(uniqKey, defaultValue));
    }

    /**
     * 根据参数标识，查询整型系统参数
     * @param uniqKey 参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    @Operation(hidden = true)
    @GetMapping("/getInteger")
    public R<Integer> getInteger(@RequestParam String uniqKey, @RequestParam(required = false) Integer defaultValue) {
        return R.success(configService.getInteger(uniqKey, defaultValue));
    }

    /**
     * 根据参数标识，查询字符型系统参数
     * @param uniqKey 参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    @Operation(hidden = true)
    @GetMapping("/getString")
    public R<String> getString(@RequestParam String uniqKey, @RequestParam(required = false) String defaultValue) {
        return R.success(configService.getString(uniqKey, defaultValue));
    }

    /**
     * 根据参数标识，查询布尔型系统参数
     * @param uniqKey 参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    @Operation(hidden = true)
    @GetMapping("/getBoolean")
    public R<Boolean> getBoolean(@RequestParam String uniqKey, @RequestParam(required = false) Boolean defaultValue) {
        return R.success(configService.getBoolean(uniqKey, defaultValue));
    }
}
