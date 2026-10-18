package top.mddata.console.facade.api.system;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.mddata.common.constant.AppConstants;
import top.mddata.console.facade.api.system.fallback.DictApiFallback;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author henhen
 * @since 2026/5/10 23:07
 */
@FeignClient(name = AppConstants.CONSOLE_SERVER, fallback = DictApiFallback.class, path = "/inner/system/dictItem")
public interface DictApi {
    /**
     * 查询字典项
     *
     * @param dictKeys 字典key
     * @return 字典key#字典项key -> 字典
     */
    @PostMapping("/findByIds")
    Map<Serializable, Object> findByIds(@RequestParam(value = "ids") Set<Serializable> dictKeys);
}
