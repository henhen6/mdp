package top.mddata.console.facade.api.system;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.console.dto.system.RequestLogDto;
import top.mddata.console.facade.api.system.fallback.RequestLogApiFallback;

/**
 *
 * @author henhen
 * @since 2026/5/10 23:07
 */
@FeignClient(name = AppConstants.CONSOLE_SERVER, fallback = RequestLogApiFallback.class, path = "/system/requestLog")
public interface RequestLogApi {
    @PostMapping("/save")
    R<Long> saveRequestLog(@RequestBody RequestLogDto dto);
}
