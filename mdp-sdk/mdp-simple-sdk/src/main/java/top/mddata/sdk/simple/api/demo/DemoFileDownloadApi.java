package top.mddata.sdk.simple.api.demo;


import top.mddata.sdk.simple.request.demo.DemoFileDownloadRequest;
import top.mddata.sdk.core.param.BaseParam;

/**
 * 下载文件
 * @author 六如
 */
public class DemoFileDownloadApi extends BaseParam<DemoFileDownloadRequest, Object> {
    @Override
    protected String method() {
        return "demo.download";
    }
}
