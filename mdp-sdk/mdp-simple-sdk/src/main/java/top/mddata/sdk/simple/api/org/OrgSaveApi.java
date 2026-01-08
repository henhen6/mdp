package top.mddata.sdk.simple.api.org;

import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.simple.request.org.OrgSaveDto;
import top.mddata.sdk.simple.response.org.OrgResp;

/**
 * 批量保存用户
 * @author henhen
 */
public class OrgSaveApi extends BaseParam<OrgSaveDto, OrgResp> {
    @Override
    protected String method() {
        return "org.save";
    }

}
