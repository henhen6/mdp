package top.mddata.sdk.simple.api.org;

import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.simple.request.org.OrgUpdateDto;
import top.mddata.sdk.simple.response.org.OrgResp;

/**
 * 修改用户
 * @author henhen
 * @since 2026/1/8 09:15
 */
public class OrgUpdateByIdApi extends BaseParam<OrgUpdateDto, OrgResp> {
    @Override
    protected String method() {
        return "org.updateById";
    }

}
