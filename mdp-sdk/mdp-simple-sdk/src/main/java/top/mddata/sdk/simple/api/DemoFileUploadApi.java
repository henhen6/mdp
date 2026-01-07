package top.mddata.sdk.simple.api;


import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.simple.request.DemoFileUploadRequest;
import top.mddata.sdk.simple.response.UserVo;

/**
 * @author 六如
 */
public class DemoFileUploadApi extends BaseParam<DemoFileUploadRequest, UserVo> {
    @Override
    protected String method() {
        return "openapi.upload.more";
    }
}
