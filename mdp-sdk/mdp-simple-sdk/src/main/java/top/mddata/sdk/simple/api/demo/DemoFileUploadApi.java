package top.mddata.sdk.simple.api.demo;


import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.simple.request.demo.DemoFileUploadRequest;
import top.mddata.sdk.simple.response.demo.DemoUploadResp;

/**
 * 上传文件
 * @author henhen
 */
public class DemoFileUploadApi extends BaseParam<DemoFileUploadRequest, DemoUploadResp> {
    @Override
    protected String method() {
        return "demo.upload.more";
    }
}
