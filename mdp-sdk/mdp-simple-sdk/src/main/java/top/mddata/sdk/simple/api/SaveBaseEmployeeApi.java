package top.mddata.sdk.simple.api;

import top.mddata.sdk.simple.request.SaveBaseEmployeeRequest;
import top.mddata.sdk.simple.response.GetBaseEmployeeResponse;
import top.mddata.sdk.core.param.BaseParam;

public class SaveBaseEmployeeApi extends BaseParam<SaveBaseEmployeeRequest, GetBaseEmployeeResponse> {
    @Override
    protected String method() {
        return "openapi.employee.save";
    }

}
