package top.mddata.gateway.sop.exception;


import com.gitee.sop.support.message.ApiResponse;
import top.mddata.gateway.sop.request.ApiRequestContext;

/**
 * @author 六如
 */
public interface ExceptionExecutor {

    ApiResponse executeException(ApiRequestContext apiRequestContext, Exception e);

}
