package top.mddata.gateway.sop.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.gateway.sop.pojo.dto.ApiDto;
import top.mddata.gateway.sop.pojo.dto.AppDto;
import top.mddata.gateway.sop.request.ApiRequestContext;

/**
 * @author 六如
 */
@Getter
@AllArgsConstructor
public class RouteContext {

    private ApiRequestContext apiRequestContext;
    private ApiDto api;
    private AppDto app;

}
