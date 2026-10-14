package top.mddata.sdk.test.demo;

import top.mddata.sdk.core.common.FileResult;
import top.mddata.sdk.core.common.Result;
import top.mddata.sdk.core.common.UploadFile;
import top.mddata.sdk.simple.api.demo.DemoFileDownloadApi;
import top.mddata.sdk.simple.api.demo.DemoFileUploadApi;
import top.mddata.sdk.simple.request.demo.DemoFileDownloadRequest;
import top.mddata.sdk.simple.request.demo.DemoFileUploadRequest;
import top.mddata.sdk.simple.response.demo.DemoUploadResp;
import top.mddata.sdk.test.BaseTest;

import java.nio.charset.StandardCharsets;

/**
 * 文件上传/下载链路基线测试。
 * 需先启动本地环境（网关 localhost:23456 + md-api + 注册中心），接口由 ApiRegisterRunner 自动注册。
 * 用途：验证 CommonsMultipartFile 拷贝类删除前后上传链路行为一致（Task 5 的判定依据）。
 *
 * @author henhen
 */
public class FileLinkTest extends BaseTest {

    /** 上传：multipart 经网关解析 → FileData 注入 → 返回文件名与大小 */
    public void testUpload() {
        getAccessToken();
        DemoFileUploadApi api = new DemoFileUploadApi();
        DemoFileUploadRequest request = new DemoFileUploadRequest()
                .setProductName("基线测试")
                .setAddTime("2026-10-13 12:00:00");
        api.setBizModel(request);
        byte[] content = "网关上传链路基线内容".getBytes(StandardCharsets.UTF_8);
        api.addFile(new UploadFile("file", "baseline.txt", content));

        Result<DemoUploadResp> result = client.execute(api);
        logResult(result);
        assertTrue("上传应成功", result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("baseline.txt", result.getData().getFileName());
        assertEquals(Integer.valueOf(content.length), result.getData().getSize());
    }

    /** 下载：FileData 经网关还原 → SDK 收文件流 */
    public void testDownload() {
        getAccessToken();
        DemoFileDownloadApi api = new DemoFileDownloadApi();
        api.setBizModel(new DemoFileDownloadRequest());

        FileResult fileResult = client.download(api);
        assertNotNull("下载应返回文件", fileResult);
        assertNotNull(fileResult.getFileData());
        String text = new String(fileResult.getFileData(), StandardCharsets.UTF_8);
        logger.info("下载内容: {}", text);
        assertEquals("hello open platform", text);
    }
}
