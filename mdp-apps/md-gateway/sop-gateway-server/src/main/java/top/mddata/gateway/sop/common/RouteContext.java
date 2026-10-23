package top.mddata.gateway.sop.common;

import lombok.Getter;
import lombok.Setter;
import top.mddata.gateway.sop.request.ApiRequestContext;
import top.mddata.open.entity.admin.ApiCallLog;

import java.util.Objects;

/**
 * @author 六如
 */
@Getter
public class RouteContext {

    private ApiRequestContext apiRequestContext;
    private ApiDto api;
    private AppDto app;
    @Setter
    private ApiCallLog apiCallLog;

    public RouteContext(ApiRequestContext apiRequestContext, ApiDto api, AppDto app, ApiCallLog apiCallLog) {
        this.apiRequestContext = apiRequestContext;
        // 在线调试等场景可能缺少应用/接口信息,兜底为空对象,避免下游拦截器 NPE
        this.api = Objects.requireNonNullElseGet(api, ApiDto::new);
        this.app = Objects.requireNonNullElseGet(app, AppDto::new);
        this.apiCallLog = apiCallLog;
    }

}
