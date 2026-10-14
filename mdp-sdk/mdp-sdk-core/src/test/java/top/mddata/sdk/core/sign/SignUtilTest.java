package top.mddata.sdk.core.sign;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SignUtilTest {

    @Test
    public void getSignCheckContent不修改调用方入参() {
        Map<String, String> params = new HashMap<>();
        params.put("b", "2");
        params.put("a", "1");
        params.put("sign", "xxx");
        params.put("sign_type", "RSA2");

        String content = SignUtil.getSignCheckContentV1(params);
        assertEquals("a=1&b=2", content);
        assertTrue("调用方入参的 sign 不应被删除", params.containsKey("sign"));
        assertTrue("调用方入参的 sign_type 不应被删除", params.containsKey("sign_type"));

        Map<String, Object> params2 = new HashMap<>();
        params2.put("b", "2");
        params2.put("a", 1);
        params2.put("sign", "xxx");
        String content2 = SignUtil.getSignCheckContentV2(params2);
        assertEquals("a=1&b=2", content2);
        assertTrue(params2.containsKey("sign"));
    }

    @Test
    public void rsaCheckV2签名缺失返回false() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("a", "1");
        assertFalse(SignUtil.rsaCheckV2(params, "anyPublicKey", "UTF-8", "RSA2"));
    }
}
