package top.mddata.sdk.test;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mddata.sdk.core.client.OpenClient;
import top.mddata.sdk.core.common.Result;
import top.mddata.sdk.simple.api.token.AccessTokenGetApi;
import top.mddata.sdk.simple.request.token.AccessTokenGetDto;
import top.mddata.sdk.simple.response.token.AccessTokenGetResp;

/**
 * 测试基类
 * @author henhen
 * @since 2026/1/8 23:44
 */
public abstract class BaseTest {
    protected Logger logger = LoggerFactory.getLogger(getClass());


    String url = System.getenv().getOrDefault("MDP_TEST_URL", "http://localhost:23456/api");
    protected String appKey = System.getenv().getOrDefault("MDP_TEST_APP_KEY", "ruoyi-vue-sso");
    /**
     * 应用秘钥，用于获取 accessToken。从环境变量读取，禁止提交真实值
     */
    protected String appSecret = System.getenv().getOrDefault("MDP_TEST_APP_SECRET", "0Tvtm2xrTWG7n2azkM4FPyHwS6c3NxjQjYKh");
    /**
     * 开发者私钥。从环境变量读取，禁止提交真实值
     */
    String privateKeyIsv = System.getenv().getOrDefault("MDP_TEST_PRIVATE_KEY", "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC/VS3h9FdV+k7YS0xwPZTGOuk0SQ4dWdFoxJ0cA3s63Rk0AjbU92zHj088pa56x0/xrlDQcZtI0cbuas7nU/bOx0eZ+0LY17EqPXmSLB1AdG8I9IEbaB+jzwPpznDI15MjZRaUpl4yr7syh1k2Ugzz8+pSUWzbb+urmmfqvn8POhYYSUrm4Zk5l+vrHE/u3bunogan3MK3eTPyx3Lt4jbPN6Y+znDcoawbTb/qZL75+YFFaCHndf/A7DJlBLlfMVxPQahUr5IInWjmECRNpRqLl5rOLQEsUWy1MiLBpAsesJxYzyejIZIuPnobEMfWkWDkgbwymIgWMzYvc7Q6hAhnAgMBAAECggEAJXHsKt6BASidqaMC8Kx8o1cAMOVjR8c+PnzMKqFbyqdeuVj9lixeM6gOX9YlEY5UTP5KfqDdPSEhB6QLniZGlS1XDAGqkXmVCKlDU6Iij2y6FkyTv+Ne3dYz89wdIpFeEH1GMA1nPhA6WKc4hHMGafAAmd+pqEB9JPZxA/YIM9hZtgk+aBUSgRVBxFPfaVXdaRjRmnCtW6hdee/GePH6R0X/ieFjIlFd+BVWsbSr3N8BKq18kWsh+pGzZXsR47aVEwwJwRrbFWjABjH0IS4wEuMQCi5B8VCEq0nb2Iz06y0IJ1Yaex5OPC7n0yjAJwgKDEsOsRIYIIOmkCv3+AM5QQKBgQDjUwY25HDT4jKBh56yKSAbZk9L9vRK6PCHwZ2yXQ0pyTPmpsqqk6VdbHmPZzs09CmSwLg/GxBdK21E/3OFJIjqLQ/+pqj/HTy7cSTqBEDNqNPE4FPz1UaWGOI8jm/Q8q+fxK0K/yGP/grWBohWKLKqZiBE7Bgd9z/DtC91ymKgsQKBgQDXd+FO0tPguQCu5dVIj7R4PLpYRAspXAMU0EFBm/XrbT1Ll4RpuBHl07AWWDjgapcWH8yF2WBq8WVCmZy2lrObfzkIJSGs0k/X1OyeTQShLFAXnF4POYh2pj3LFj8s9MHO8ah7ix1m2qH6BjsD3dlDKUqc1idzh5U3kJKaF5tAlwKBgQCYIZDwHXNQqXlpbCyNSK5/B7obuXqFw1xtTerOWi2cAFXmj0rkWwj4+8ZibRCXgKtt1eG4AdGyuIRY/6f8u5WROnUQ09IXYSaqvq6Ymh4QRGLsx8AHV3z0qFSHeD9mk3NrNcEksddxOO9hil+lYXkoRk5kMah2LWiT/Tsh1j6pEQKBgQC2O4mvJNhWA6H0SiYtDH1SA+qGpGXcQRnKDKhkWQeQaf+hYzB2SVu5yWPwQgU4qG3IJHTR75uAV1GRFmJYevTE2sDdhqoIhIdKv6av6+uydMv4bCORNNOZpdg1X0dnOkqAQBqDApGHX/oGgCaBiqwqBU45f1Y2e8FUEU4sTTLdWQKBgHFcgjZ5VZcS+1m+pVuHPxQx7anYodjknKCE/sB7Cw3pC0CsV0Etwn1Yg3MM6oAicJpqURYUyXKqn1L43xGiqlEn0CWUphGzf12Q9+W90QAPsItDfUXuJVnDOeOqUQZd9ryDiX/Gdf4rj4E2JjlYqP+/n4dI7g9dGDhWMRjhrWtk");

    // 声明一个就行
    protected OpenClient client = new OpenClient(url, appKey, privateKeyIsv);

    /**
     * 获取访问令牌并设置到客户端
     *
     * @return 访问令牌
     */
    protected String getAccessToken() {
        AccessTokenGetApi api = new AccessTokenGetApi();
        AccessTokenGetDto dto = new AccessTokenGetDto()
                .setAppKey(appKey)
                .setAppSecret(appSecret);
        api.setBizModel(dto);

        Result<AccessTokenGetResp> result = client.execute(api);
        if (result.isSuccess() && result.getData() != null) {
            String token = result.getData().getAccessToken();
            client.setDefaultAccessToken(token);
            logger.info("获取accessToken成功: {}...", token.substring(0, Math.min(20, token.length())));
            return token;
        } else {
            logger.error("获取accessToken失败: {}", JSON.toJSONString(result));
            return null;
        }
    }

    protected void logResult(Result<?> result) {
        if (result.isSuccess()) {
            // 返回结果
            logger.info("调用成功 : {}", JSON.toJSONString(result));
        } else {
            logger.error("调用错误，subCode: {}, subMsg:{}", JSON.toJSONString(result), result.getSubMsg());
        }
    }

}
