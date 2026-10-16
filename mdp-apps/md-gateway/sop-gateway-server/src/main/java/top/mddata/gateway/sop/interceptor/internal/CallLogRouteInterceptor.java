package top.mddata.gateway.sop.interceptor.internal;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baidu.fsg.uid.UidGenerator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import top.mddata.gateway.sop.common.ApiDto;
import top.mddata.gateway.sop.common.AppDto;
import top.mddata.gateway.sop.common.RouteContext;
import top.mddata.gateway.sop.interceptor.RouteInterceptor;
import top.mddata.gateway.sop.interceptor.RouteInterceptorOrders;
import top.mddata.gateway.sop.request.ApiRequestContext;
import top.mddata.open.entity.admin.ApiCallLog;
import top.mddata.open.enumeration.admin.ExecStatusEnum;

import java.time.LocalDateTime;


/**
 * 调用日志
 *
 * @author henhen
 */
@Component
public class CallLogRouteInterceptor implements RouteInterceptor {

    @Resource
    private UidGenerator uidGenerator;

    @Override
    public void preRoute(RouteContext routeContext) {
        ApiRequestContext apiRequestContext = routeContext.getApiRequestContext();
        AppDto app = routeContext.getApp();
        ApiDto api = routeContext.getApi();
        ApiCallLog callLog = new ApiCallLog();
        callLog.setTraceId(apiRequestContext.getTraceId());
        BeanUtil.copyProperties(api, callLog);
        BeanUtil.copyProperties(app, callLog);
        callLog.setAppKey(app.getAppKey())
                .setAppName(app.getName())
                .setAppId(app.getId())
                .setApiId(api.getId())
                .setRequestIp(apiRequestContext.getIp())
                .setParamInfo(JSON.toJSONString(apiRequestContext.getApiRequest()))
                .setRequestTime(LocalDateTime.now())
                .setExecStatus(ExecStatusEnum.WAIT.getCode())
                .setId(uidGenerator.getUid());

        routeContext.setApiCallLog(callLog);
    }

    @Override
    public Object afterRoute(RouteContext routeContext, Object result) {
        ApiCallLog apiCallLog = routeContext.getApiCallLog();
        if (apiCallLog != null) {
            apiCallLog.setResponseTime(LocalDateTime.now());
            apiCallLog.setExecStatus(ExecStatusEnum.SUCCESS.getCode());
            apiCallLog.setResponseData(result == null ? null : JSON.toJSONString(result));

            routeContext.setApiCallLog(apiCallLog);
        }

        return result;
    }

    @Override
    public int getOrder() {
        return RouteInterceptorOrders.CALL_LOG_INTERCEPTOR;
    }
}
