package top.mddata.sdk.test.demo;

import com.alibaba.fastjson2.JSON;
import junit.framework.TestCase;
import top.mddata.sdk.core.client.OpenClient;
import top.mddata.sdk.core.common.Result;
import top.mddata.sdk.simple.api.token.AccessTokenGetApi;
import top.mddata.sdk.simple.api.user.UserBatchSaveApi;
import top.mddata.sdk.simple.request.token.AccessTokenGetDto;
import top.mddata.sdk.simple.request.user.UserBatchSaveDto;
import top.mddata.sdk.simple.request.user.UserSaveDto;
import top.mddata.sdk.simple.response.token.AccessTokenGetResp;
import top.mddata.sdk.simple.response.user.UserBatchSaveResp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 回调接口测试类
 * @author henhen
 * @since 2025/12/18 00:21
 */
public class NotifyTest extends TestCase {
    String url = System.getenv().getOrDefault("MDP_TEST_URL", "http://localhost:23456/api");
    String appKey = System.getenv().getOrDefault("MDP_TEST_APP_KEY", "ruoyi-vue-sso");
    /**
     * 应用秘钥，用于获取 accessToken。从环境变量读取，禁止提交真实值
     */
    String appSecret = System.getenv().getOrDefault("MDP_TEST_APP_SECRET", "请配置环境变量MDP_TEST_APP_SECRET");
    /**
     * 开发者私钥。从环境变量读取，禁止提交真实值
     */
    String privateKeyIsv = System.getenv().getOrDefault("MDP_TEST_PRIVATE_KEY", "请配置环境变量MDP_TEST_PRIVATE_KEY");

    // 声明一个就行
    OpenClient client = new OpenClient(url, appKey, privateKeyIsv);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // 获取 accessToken
        getAccessToken();
    }

    /**
     * 获取访问令牌
     */
    private void getAccessToken() {
        AccessTokenGetApi api = new AccessTokenGetApi();
        AccessTokenGetDto dto = new AccessTokenGetDto()
                .setAppKey(appKey)
                .setAppSecret(appSecret);
        api.setBizModel(dto);

        Result<AccessTokenGetResp> result = client.execute(api);
        if (result.isSuccess() && result.getData() != null) {
            String token = result.getData().getAccessToken();
            client.setDefaultAccessToken(token);
            System.out.printf("获取accessToken成功: %s...%n", token.substring(0, Math.min(20, token.length())));
        } else {
            System.err.println("获取accessToken失败: " + JSON.toJSONString(result));
        }
    }

    /**
     * 测试 查询员工
     */
    public void testSaveBaseEmployee() {
        List<UserSaveDto> list = new ArrayList<>();
        UserSaveDto user1 = new UserSaveDto();
        user1.setUsername("test" + new Random().nextInt(5000));
        user1.setUserSource("111");
        user1.setName("test1");
        list.add(user1);
        user1 = new UserSaveDto();
        user1.setUsername("test" + new Random().nextInt(5000));
        user1.setName("test2");
        user1.setUserSource("111");
        list.add(user1);

        UserBatchSaveDto dto = new UserBatchSaveDto();
        dto.setList(list);
        // 创建请求对象
        UserBatchSaveApi param = new UserBatchSaveApi();
        param.setBizModel(dto);
        // 这里提供了 mdp-api-server 的一个接口地址作为测试
        param.setNotifyUrl("http://localhost:18766/notify/callback22333");

        // 发送请求
        Result<UserBatchSaveResp> result = client.execute(param);

        if (result.isSuccess()) {
            UserBatchSaveResp response = result.getData();
            // 返回结果
            System.err.printf("调用成功:%s%n", JSON.toJSONString(result));
        } else {
            System.err.println("调用错误，subCode:" + JSON.toJSONString(result) + ", subMsg:" + result.getSubMsg());
        }
    }

}
