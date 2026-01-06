package top.mddata.gateway.sop.service.validate.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.StringUtils;
import top.mddata.gateway.sop.exception.ApiException;
import top.mddata.gateway.sop.message.ErrorEnum;
import top.mddata.gateway.sop.request.ApiRequest;
import top.mddata.gateway.sop.request.ApiRequestContext;
import top.mddata.gateway.sop.service.validate.Signer;

/**
 * @author 六如
 */
@Slf4j
public abstract class AbstractSigner implements Signer {

    protected static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex);
        }
        return sign.toString();
    }

    /**
     * 构建服务端签名串
     *
     * @param apiRequestContext 接口参数
     * @param secret 秘钥
     * @return 返回服务端签名串
     */
    protected abstract String buildServerSign(ApiRequestContext apiRequestContext, String secret);

    @Override
    public boolean checkSign(ApiRequestContext apiRequestContext, String publicKey) {
        ApiRequest apiRequest = apiRequestContext.getApiRequest();
        String clientSign = apiRequest.getSign();
        if (StringUtils.isBlank(clientSign)) {
            throw new ApiException(ErrorEnum.ISV_MISSING_SIGNATURE, apiRequestContext.getLocale());
        }
        String serverSign = buildServerSign(apiRequestContext, publicKey);
        return clientSign.equals(serverSign);
    }
}
