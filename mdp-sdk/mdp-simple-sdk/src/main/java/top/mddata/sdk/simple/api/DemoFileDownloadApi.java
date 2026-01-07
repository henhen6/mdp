package top.mddata.sdk.simple.api;


import top.mddata.sdk.simple.request.DemoFileDownloadRequest;
import top.mddata.sdk.core.param.BaseParam;

/**
 * @author 六如
 */
public class DemoFileDownloadApi extends BaseParam<DemoFileDownloadRequest, Object> {
    @Override
    protected String method() {
        return "openapi.download";
    }
}
