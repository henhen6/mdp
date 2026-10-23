package top.mddata.console.facade.api.system;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.console.facade.api.system.fallback.ConfigApiFallback;
import top.mddata.console.vo.system.ConfigVo;

/**
 *
 * @author henhen
 * @since 2026/5/10 23:07
 */
@FeignClient(name = AppConstants.CONSOLE_SERVER, fallback = ConfigApiFallback.class, path = "/inner/system/config")
public interface ConfigApi {
    /**
     * 根据参数标识，查询系统参数VO
     * @param uniqKey 参数标识
     * @return 系统参数
     */
    @GetMapping("/getConfig")
    R<ConfigVo> getConfig(@RequestParam String uniqKey);

    /**
     * 根据参数标识，查询长整型系统参数
     * @param uniqKey 参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    @GetMapping("/getLong")
    R<Long> getLong(@RequestParam String uniqKey, @RequestParam(required = false) Long defaultValue);

    /**
     * 根据参数标识，查询整型系统参数
     * @param uniqKey 参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    @GetMapping("/getInteger")
    R<Integer> getInteger(@RequestParam String uniqKey, @RequestParam(required = false) Integer defaultValue);

    /**
     * 根据参数标识，查询字符型系统参数
     * @param uniqKey 参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    @GetMapping("/getString")
    R<String> getString(@RequestParam String uniqKey, @RequestParam(required = false) String defaultValue);

    /**
     * 根据参数标识，查询布尔型系统参数
     * @param uniqKey 参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    @GetMapping("/getBoolean")
    R<Boolean> getBoolean(@RequestParam String uniqKey, @RequestParam(required = false) Boolean defaultValue);
}
