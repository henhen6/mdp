package top.mddata.console.facade.api.system.fallback;

import org.springframework.stereotype.Component;
import top.mddata.base.base.R;
import top.mddata.console.dto.system.RequestLogDto;
import top.mddata.console.facade.api.system.RequestLogApi;

/**
 *
 * @author henhen
 * @since 2026/5/10 23:20
 */
@Component
public class RequestLogApiFallback implements RequestLogApi {
    @Override
    public R<Long> saveRequestLog(RequestLogDto dto) {
        return R.timeout();
    }
}
