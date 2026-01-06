package top.mddata.gateway.sop.service.validate;

import top.mddata.gateway.sop.request.ApiRequestContext;

/**
 * 负责签名校验
 *
 * @author 六如
 */
public interface Signer {

    /**
     * 签名校验
     *
     * @param apiRequestContext 参数
     * @param publicKey         公钥
     * @return true签名正确
     */
    boolean checkSign(ApiRequestContext apiRequestContext, String publicKey);

}
