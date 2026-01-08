package top.mddata.sdk.simple.api.org;


import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.simple.request.org.OrgGetByIdDto;
import top.mddata.sdk.simple.response.org.OrgResp;

/**
 * 获取用户信息
 * @author henhen
 */
public class OrgGetByIdApi extends BaseParam<OrgGetByIdDto, OrgResp> {
    @Override
    protected String method() {
        return "org.getById";
    }
}