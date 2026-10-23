//package top.mddata.sdk.test.demo;
//
//import junit.framework.TestCase;
//import top.mddata.sdk.simple.api.demo.DemoFileDownloadApi;
//import top.mddata.sdk.simple.request.demo.DemoFileDownloadRequest;
//import top.mddata.sdk.core.client.OpenClient;
//import top.mddata.sdk.core.common.Result;
//
///**
// * 测试SDK
// */
//public class SdkTest extends TestCase {
//    String url = System.getenv().getOrDefault("MDP_TEST_URL", "http://localhost:23456/api");
//    String appKey = System.getenv().getOrDefault("MDP_TEST_APP_KEY", "ruoyi-vue-oauth");
//    /**
//     * 开发者私钥。从环境变量读取，禁止提交真实值
//     */
//    String privateKeyIsv = System.getenv().getOrDefault("MDP_TEST_PRIVATE_KEY", "请配置环境变量MDP_TEST_PRIVATE_KEY");
//    /**
//     * 开放平台提供的公钥
//     * 前往SOP-ADMIN，ISV管理--秘钥管理，生成平台提供的公私钥，然后把【平台公钥】放到这里
//     */
//    String publicKeyPlatform = "";
//
//    // 声明一个就行
//    OpenClient client = new OpenClient(url, appKey, privateKeyIsv, publicKeyPlatform);
//
//
//    public void testDownload() {
//        DemoFileDownloadApi request = new DemoFileDownloadApi();
//        DemoFileDownloadRequest model = new DemoFileDownloadRequest();
//        model.setId(123);
//        request.setBizModel(model);
//        Result<Object> execute = client.execute(request);
//
//        System.out.println(execute);
//    }
//
//}
