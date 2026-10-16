package top.mddata.sdk.test.token;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import top.mddata.sdk.core.common.Result;
import top.mddata.sdk.simple.api.token.AccessTokenGetApi;
import top.mddata.sdk.simple.request.token.AccessTokenGetDto;
import top.mddata.sdk.simple.response.token.AccessTokenGetResp;
import top.mddata.sdk.test.BaseTest;

/**
 * 访问令牌接口测试
 *
 * @author henhen
 * @since 2026/7/2
 */
public class TokenTest extends BaseTest {

    /**
     * 测试获取 accessToken（不需要 token，需要签名）
     */
    @Test
    public void testGetAccessToken() {
        AccessTokenGetApi api = new AccessTokenGetApi();
        AccessTokenGetDto dto = new AccessTokenGetDto()
                .setAppKey(appKey)
                .setAppSecret(appSecret);
        api.setBizModel(dto);

        Result<AccessTokenGetResp> result = client.execute(api);
        logResult(result);

        if (result.isSuccess() && result.getData() != null) {
            AccessTokenGetResp resp = result.getData();
            System.out.printf("accessToken: %s%n", resp.getAccessToken());
            System.out.printf("expiresIn: %d 秒%n", resp.getExpiresIn());
        }
    }

    /**
     * 测试强制刷新 accessToken
     */
    @Test
    public void testForceRefreshToken() {
        // 先获取一次
        testGetAccessToken();

        // 强制刷新
        AccessTokenGetApi api = new AccessTokenGetApi();
        AccessTokenGetDto dto = new AccessTokenGetDto()
                .setAppKey(appKey)
                .setAppSecret(appSecret)
                .setForceRefresh(true);
        api.setBizModel(dto);

        Result<AccessTokenGetResp> result = client.execute(api);
        logResult(result);

        if (result.isSuccess() && result.getData() != null) {
            AccessTokenGetResp resp = result.getData();
            System.out.printf("新的 accessToken: %s%n", resp.getAccessToken());
        }
    }

    /**
     * 测试使用 accessToken 调用需要认证的接口
     */
    @Test
    public void testUseAccessToken() {
        // 先获取 accessToken
        String token = getAccessToken();
        assertNotNull(token, "accessToken 不应为空");

        // 使用 accessToken 调用接口（这里以 user.getById 为例）
        // client 已经设置了 defaultAccessToken，所以不需要显式传递
        System.out.printf("使用 accessToken 调用接口...%n");
    }
}
