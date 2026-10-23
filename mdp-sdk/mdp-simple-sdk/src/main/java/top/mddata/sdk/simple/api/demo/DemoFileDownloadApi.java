package top.mddata.sdk.simple.api.demo;

import top.mddata.sdk.core.param.DownloadRequest;
import top.mddata.sdk.simple.request.demo.DemoFileDownloadRequest;

/**
 * 下载文件。
 * 必须继承 DownloadRequest（实现 DownloadAware），OpenClient 才会走文件下载分支而非 JSON 解析分支。
 *
 * @author 六如
 */
public class DemoFileDownloadApi extends DownloadRequest<DemoFileDownloadRequest> {
    @Override
    protected String method() {
        return "demo.download";
    }
}
