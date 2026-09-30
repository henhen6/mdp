package top.mddata.gateway.sop.interceptor.internal;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.common.enumeration.BooleanEnum;
import top.mddata.gateway.sop.common.ApiDto;
import top.mddata.gateway.sop.common.RouteContext;
import top.mddata.gateway.sop.exception.ApiException;
import top.mddata.gateway.sop.interceptor.RouteInterceptor;
import top.mddata.gateway.sop.manager.AppManager;
import top.mddata.gateway.sop.message.ErrorEnum;
import top.mddata.gateway.sop.request.ApiRequestContext;

/**
 * 校验token
 *
 * @author 六如
 */
@Component
@Slf4j
public class TokenValidateInterceptor implements RouteInterceptor {

    @Resource
    private AppManager appManager;

    @Override
    public void preRoute(RouteContext routeContext) {
        ApiRequestContext apiRequestContext = routeContext.getApiRequestContext();
        ApiDto apiInfo = routeContext.getApi();
        // 走到这里token肯定有值
        String appKey = apiRequestContext.getApiRequest().getAppKey();
        String accessToken = apiRequestContext.getApiRequest().getAccessToken();

        if (!checkToken(appKey, accessToken, apiRequestContext, apiInfo)) {
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
     * 校验token是否合法（从 Redis 查询 {appKey}:{token}→appId 映射是否存在）。
     * <p>
     * 使用复合 key 将 token 与 appKey 绑定校验，防止 A 应用的 token
     * 被用于冒充 B 应用调用接口。
     *
     * @param appKey      应用标识
     * @param accessToken 访问令牌
     * @param context     上下文
     * @param apiInfoDTO  接口信息
     * @return 返回true表示token合法，false不合法
     */
    protected boolean checkToken(String appKey, String accessToken, ApiRequestContext context, ApiDto apiInfoDTO) {
        Long appId = appManager.getAppIdByAccessToken(appKey, accessToken);
        if (appId == null) {
            log.warn("accessToken无效、已过期或与当前应用不匹配, appKey={}, method={}",
                    appKey, apiInfoDTO.getMethodName());
            return false;
        }
        return true;
    }
}
