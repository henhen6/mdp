package top.mddata.gateway.sop.interceptor.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.common.enumeration.BooleanEnum;
import top.mddata.gateway.sop.common.RouteContext;
import top.mddata.gateway.sop.exception.ApiException;
import top.mddata.gateway.sop.interceptor.RouteInterceptor;
import top.mddata.gateway.sop.message.ErrorEnum;
import top.mddata.gateway.sop.common.ApiDto;
import top.mddata.gateway.sop.request.ApiRequestContext;

/**
 * 校验token
 *
 * @author 六如
 */
@Component
@Slf4j
public class TokenValidateInterceptor implements RouteInterceptor {
    @Override
    public void preRoute(RouteContext routeContext) {
        ApiRequestContext apiRequestContext = routeContext.getApiRequestContext();
        ApiDto apiInfo = routeContext.getApi();
        // 走到这里token肯定有值
        String appAuthToken = apiRequestContext.getApiRequest().getAppAuthToken();

        if (!checkToken(appAuthToken, apiRequestContext, apiInfo)) {
            throw new ApiException(ErrorEnum.AOP_INVALID_AUTH_TOKEN, apiRequestContext.getLocale());
        }
    }

    @Override
    public boolean match(RouteContext routeContext) {
        ApiDto apiInfo = routeContext.getApi();
        Integer isNeedToken = apiInfo.getNeedToken();
        return BooleanEnum.TRUE.eq(isNeedToken);
    }

    /**
     * 校验token是否合法
     *
     * @param appAuthToken token
     * @param context      上下文
     * @param apiInfoDTO   接口信息
     * @return 返回true表示token合法，false不合法
     */
    protected boolean checkToken(String appAuthToken, ApiRequestContext context, ApiDto apiInfoDTO) {
        // 这里做校验token操作，如从redis查询token是否存在

        return true;
    }
}
