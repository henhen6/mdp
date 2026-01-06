package top.mddata.gateway.sop.service;

import top.mddata.gateway.sop.common.RouteContext;
import top.mddata.gateway.sop.response.Response;

import java.util.Optional;

/**
 * 结果包裹
 *
 * @author 六如
 */
public interface ResultWrapper {

    Response wrap(Optional<RouteContext> routeContextOpt, Object result);

}
