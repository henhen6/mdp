package top.mddata.gateway.sop.service.validate;


import top.mddata.gateway.sop.request.ApiRequestContext;

/**
 * 校验接口
 *
 * @author 六如
 */
public interface SopValidator {
    /**
     * 接口验证
     *
     * @param apiRequestContext 请求内容
     * @return 校验通过返回路由信息
     */
    ValidateReturn validate(ApiRequestContext apiRequestContext);

    /**
     * 校验rest
     *
     * @param apiRequestContext 请求内容
     * @return 校验通过返回路由信息
     */
    ValidateReturn validateRest(ApiRequestContext apiRequestContext);

}
