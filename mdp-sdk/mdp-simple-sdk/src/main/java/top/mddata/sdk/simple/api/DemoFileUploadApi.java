package top.mddata.sdk.simple.api;


import top.mddata.sdk.simple.request.DemoFileUploadRequest;
import top.mddata.sdk.simple.response.GetBaseEmployeeResponse;
import top.mddata.sdk.core.param.BaseParam;

/**
 * @author 六如
 */
public class DemoFileUploadApi extends BaseParam<DemoFileUploadRequest, GetBaseEmployeeResponse> {
    @Override
    protected String method() {
        return "openapi.upload.more";
    }
}
