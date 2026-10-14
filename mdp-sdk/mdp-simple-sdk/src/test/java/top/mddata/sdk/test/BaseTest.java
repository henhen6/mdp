package top.mddata.sdk.test;

import com.alibaba.fastjson2.JSON;
import junit.framework.TestCase;
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
public abstract class BaseTest extends TestCase {
    protected Logger logger = LoggerFactory.getLogger(getClass());


    String url = System.getenv().getOrDefault("MDP_TEST_URL", "http://localhost:23456/api");
    protected String appKey = System.getenv().getOrDefault("MDP_TEST_APP_KEY", "ruoyi-vue-sso");
    /**
     * 应用秘钥，用于获取 accessToken。从环境变量读取，禁止提交真实值
     */
    protected String appSecret = System.getenv().getOrDefault("MDP_TEST_APP_SECRET", "请配置环境变量MDP_TEST_APP_SECRET");
    /**
     * 开发者私钥。从环境变量读取，禁止提交真实值
     */
    String privateKeyIsv = System.getenv().getOrDefault("MDP_TEST_PRIVATE_KEY", "请配置环境变量MDP_TEST_PRIVATE_KEY");

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
