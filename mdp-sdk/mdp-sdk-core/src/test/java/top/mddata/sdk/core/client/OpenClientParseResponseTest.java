package top.mddata.sdk.core.client;

import org.junit.Test;
import top.mddata.sdk.core.common.Result;
import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.core.response.Page;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * OpenClient.parseResponse 泛型反序列化测试
 */
public class OpenClientParseResponseTest {

    /**
     * 测试用响应模型
     */
    public static class UserResp {
        private String name;

        public String getName() {
            return name;
        }

        public UserResp setName(String name) {
            this.name = name;
            return this;
        }
    }

    /**
     * 分页响应接口：Resp 为参数化类型 Page&lt;UserResp&gt;
     */
    private static class UserPageApi extends BaseParam<Object, Page<UserResp>> {
        @Override
        protected String method() {
            return "user.page";
        }
    }

    /**
     * 普通对象响应接口
     */
    private static class UserGetApi extends BaseParam<Object, UserResp> {
        @Override
        protected String method() {
            return "user.get";
        }
    }

    private final OpenClient client = new OpenClient("http://localhost/api", "appKey", "privateKey");

    @Test
    public void parsePageResponseKeepsElementType() {
        String resp = "{\"code\":\"0\",\"data\":{\"records\":[{\"name\":\"张三\"}],"
                + "\"pageNumber\":1,\"pageSize\":15,\"totalRow\":1}}";
        Result<Page<UserResp>> result = client.parseResponse(resp, new UserPageApi());
        List<UserResp> records = result.getData().getRecords();
        assertEquals(1, records.size());
        assertTrue("records 元素应为 UserResp, 实际=" + records.get(0).getClass().getName(),
                records.get(0) instanceof UserResp);
        assertEquals("张三", records.get(0).getName());
    }

    @Test
    public void parsePlainObjectResponse() {
        String resp = "{\"code\":\"0\",\"data\":{\"name\":\"李四\"}}";
        Result<UserResp> result = client.parseResponse(resp, new UserGetApi());
        assertEquals("李四", result.getData().getName());
    }
}
