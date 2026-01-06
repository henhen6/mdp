package top.mddata.gateway.sop.service.impl;

import com.alibaba.fastjson2.JSONObject;
import top.mddata.gateway.sop.request.ApiRequestContext;
import org.springframework.stereotype.Service;
import top.mddata.gateway.sop.service.validate.ValidateReturn;

/**
 * @author 六如
 */
@Service("restRouteService")
public class RestRouteServiceImpl extends RouteServiceImpl {

    @Override
    protected ValidateReturn validate(ApiRequestContext apiRequestContext) {
        return validator.validateRest(apiRequestContext);
    }

    @Override
    protected JSONObject getParamObject(ApiRequestContext apiRequestContext) {
        return apiRequestContext.getRawParams();
    }
}
