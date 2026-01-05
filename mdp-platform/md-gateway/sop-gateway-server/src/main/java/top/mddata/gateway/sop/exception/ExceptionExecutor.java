package top.mddata.gateway.sop.exception;


import top.mddata.gateway.sop.request.ApiRequestContext;
import top.mddata.gateway.sop.response.ApiResponse;

/**
 * @author 六如
 */
public interface ExceptionExecutor {

    ApiResponse executeException(ApiRequestContext apiRequestContext, Exception e);

}
