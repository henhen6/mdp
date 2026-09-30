package top.mddata.gateway.sop.service.impl;

import com.gitee.sop.support.message.ApiResponse;
import com.gitee.sop.support.message.Response;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.mddata.common.enumeration.BooleanEnum;
import top.mddata.gateway.sop.common.ApiDto;
import top.mddata.gateway.sop.common.RouteContext;
import top.mddata.gateway.sop.config.GateApiConfig;
import top.mddata.gateway.sop.response.NoCommonResponse;
import top.mddata.gateway.sop.service.ResultWrapper;

import java.util.Optional;

/**
 * @author 六如
 */
@Service
public class ResultWrapperImpl implements ResultWrapper {

    @Resource
    private GateApiConfig apiConfig;

    @Override
    public Response wrap(Optional<RouteContext> routeContextOpt, Object result) {
        boolean needNotWrap = routeContextOpt.map(RouteContext::getApi)
                                      .map(ApiDto::getCommonResponse)
                                      .map(BooleanEnum::of)
                                      .orElse(BooleanEnum.TRUE) == BooleanEnum.FALSE;
        if (result instanceof ApiResponse apiResponse) {
            return executeApiResponse(apiResponse, needNotWrap);
        }
        // 不需要公共返回参数
        if (needNotWrap) {
            return NoCommonResponse.success(result);
        }
        return ApiResponse.success(result);
    }

    private Response executeApiResponse(ApiResponse apiResponse, boolean needNotWrap) {
        // 不需要公共返回参数
        if (needNotWrap) {
            return NoCommonResponse.success(apiResponse.getData());
        }
        return apiResponse;
    }

}
