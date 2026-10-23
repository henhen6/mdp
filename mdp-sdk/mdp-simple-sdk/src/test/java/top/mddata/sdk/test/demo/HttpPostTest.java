package top.mddata.sdk.test.demo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import top.mddata.sdk.core.client.OpenHttp;
import top.mddata.sdk.core.sign.SignUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 测试 Http 请求
 */
public class HttpPostTest {
    static OpenHttp httpTool = new OpenHttp();

    String url = System.getenv().getOrDefault("MDP_TEST_URL", "http://localhost:23456/api");
    String appKey = System.getenv().getOrDefault("MDP_TEST_APP_KEY", "ruoyi-vue-sso");
    /**
     * 应用秘钥，用于获取 accessToken。从环境变量读取，禁止提交真实值
     */
    protected String appSecret = System.getenv().getOrDefault("MDP_TEST_APP_SECRET", "请配置环境变量MDP_TEST_APP_SECRET");
    /** 平台提供的私钥。从环境变量读取，禁止提交真实值 */
    String privateKey = System.getenv().getOrDefault("MDP_TEST_PRIVATE_KEY", "请配置环境变量MDP_TEST_PRIVATE_KEY");

    public static String postJson(String url, Map<String, String> params) {
        return postJson(url, params, Collections.emptyMap());
    }

    public static String postJson(String url, Map<String, String> params, Map<String, String> headers) {
        try {
            return httpTool.requestJson(url, JSON.toJSONString(params), headers);
        } catch (IOException e) {
            throw new RuntimeException("网络请求异常", e);
        }
    }

    protected static String buildUrlQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            try {
                sb.append("&").append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString().substring(1);
    }

    protected static String buildParamQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString().substring(1);
    }

    @Test
    public void testToken() throws Exception {
        getToken();
    }

    private String getToken() throws Exception {
        // 公共请求参数
        Map<String, String> params = new HashMap<String, String>();
        params.put("appKey", appKey);
        params.put("method", "accessToken.get");
        params.put("version", "1.0");
        params.put("format", "json");
        params.put("charset", "utf-8");
        params.put("signType", "RSA2");
        params.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        // 业务参数
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("appKey", appKey);
        bizContent.put("appSecret", appSecret);

        params.put("bizContent", JSON.toJSONString(bizContent));
        String content = SignUtil.getSignContent(params);

        String sign = SignUtil.rsaSign(content, privateKey, params.get("charset"), params.get("signType"));
        params.put("sign", sign);

        System.out.println("----------- 请求信息 -----------");
        System.out.println("请求参数：" + buildParamQuery(params));
        System.out.println("商户秘钥：" + privateKey);
        System.out.println("待签名内容：" + content);
        System.out.println("签名(sign)：" + sign);
        System.out.println("URL参数：" + buildUrlQuery(params));

        System.out.println("----------- 返回结果 -----------");
        String responseData = postJson(url, params);// 发送请求
        System.out.println(responseData);

        JSONObject jsonObject = JSON.parseObject(responseData);
        JSONObject data = jsonObject.getJSONObject("data");
        return data.getString("accessToken");
    }


    /**
     参数	            类型	    是否必填	    最大长度	    描述	            示例值
     appKey	        String	是	        32	    平台分配给开发者的应用ID	2014072300007148
     method	        String	是	        128	    接口名称	alipay.trade.fastpay.refund.query
     version	        String	是	        3	    调用的接口版本，固定为：1.0	1.0
     format	        String	否	        40	    仅支持JSON	JSON
     charset	        String	是	        10	    请求使用的编码格式，如utf-8,gbk,gb2312等	utf-8
     signType	    String	是	        10	    商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2	RSA2
     sign	        String	是	        344	    商户请求参数的签名串，详见签名	详见示例
     timestamp	    String	是	        19	    发送请求的时间，格式"yyyy-MM-dd HH:mm:ss"	2014-07-24 03:07:50
     accessToken	String	否	        512	    访问令牌，通过 accessToken.get 接口获取
     bizContent	    String	是		请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递，具体参照各产品快速接入文档
     */
    // 这个请求会路由到story服务
    @Test
    public void testGetJson() throws Exception {

        String token = getToken();

        // 公共请求参数
        Map<String, String> params = new HashMap<String, String>();
        params.put("appKey", appKey);
        params.put("method", "user.getById");
        params.put("format", "json");
        params.put("charset", "utf-8");
        params.put("signType", "RSA2");
        params.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        params.put("version", "1.0");
        params.put("accessToken", token);

        // 业务参数
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("id", "45946774154457094");

        params.put("bizContent", JSON.toJSONString(bizContent));

        System.out.println("----------- 请求信息 -----------");
        System.out.println("请求参数：" + buildParamQuery(params));
        System.out.println("商户秘钥：" + privateKey);
        System.out.println("URL参数：" + buildUrlQuery(params));

        System.out.println("----------- 返回结果 -----------");
        String responseData = postJson(url, params);// 发送请求
        System.out.println(responseData);
    }

    // 输出返回xml格式
    @Test
    public void testGetXml() throws Exception {

        String token = getToken();
        // 公共请求参数
        Map<String, String> params = new HashMap<String, String>();
        params.put("appKey", appKey);
        params.put("method", "user.getById");
        params.put("version", "1.0");
        params.put("format", "xml");
        params.put("charset", "utf-8");
        params.put("signType", "RSA2");
        params.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        params.put("accessToken", token);

        // 业务参数
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("id", "45946774154457094");

        params.put("bizContent", JSON.toJSONString(bizContent));

        System.out.println("----------- 请求信息 -----------");
        System.out.println("请求参数：" + buildParamQuery(params));
        System.out.println("商户秘钥：" + privateKey);
        System.out.println("URL参数：" + buildUrlQuery(params));

        System.out.println("----------- 返回结果 -----------");
        String responseData = postJson(url, params);// 发送请求
        System.out.println(responseData);
    }

}
