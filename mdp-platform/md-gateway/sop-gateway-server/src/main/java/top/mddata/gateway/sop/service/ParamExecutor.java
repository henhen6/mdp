package top.mddata.gateway.sop.service;


import top.mddata.gateway.sop.request.ApiRequestContext;
import top.mddata.gateway.sop.response.Response;

import java.io.IOException;

/**
 * 参数处理
 *
 * @param <Req>  请求参数
 * @param <Resp> 响应参数
 * @author 六如
 */
public interface ParamExecutor<Req, Resp> {

    /**
     * 构建请求参数上下文
     *
     * @param request request
     * @return 返回请求参数上下文
     */
    ApiRequestContext build(Req request);

    /**
     * 结果返回写入
     *
     * @param apiRequestContext 请求参数上下文
     * @param apiResponse       最终返回结果
     * @param response          response
     * @throws IOException IOException
     */
    void write(ApiRequestContext apiRequestContext, Response apiResponse, Resp response) throws IOException;

}
