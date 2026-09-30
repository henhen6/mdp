package top.mddata.gateway.sop.service;

import com.gitee.sop.support.message.Response;
import top.mddata.gateway.sop.common.RouteContext;

import java.util.Optional;

/**
 * 结果包裹
 *
 * @author 六如
 */
public interface ResultWrapper {

    Response wrap(Optional<RouteContext> routeContextOpt, Object result);

}
