package top.mddata.console.facade.impl.system;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.console.dto.system.RequestLogDto;
import top.mddata.console.facade.api.system.RequestLogApi;
import top.mddata.console.facade.system.RequestLogFacade;

/**
 *
 * @author henhen
 * @since 2026/5/8 14:25
 */
@Service
@RequiredArgsConstructor
public class RequestLogFacadeImpl implements RequestLogFacade {
    private final RequestLogApi requestLogApi;

    @Override
    public void saveRequestLog(RequestLogDto dto) {
        requestLogApi.saveRequestLog(dto);
    }
}
