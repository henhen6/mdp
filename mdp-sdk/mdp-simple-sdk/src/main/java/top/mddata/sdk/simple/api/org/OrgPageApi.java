package top.mddata.sdk.simple.api.org;

import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.core.request.PageParams;
import top.mddata.sdk.core.response.Page;
import top.mddata.sdk.simple.request.org.OrgQuery;
import top.mddata.sdk.simple.response.org.OrgResp;

/**
 * 分页查询用户信息
 * <p>
 * org.page
 *
 * @author henhen
 */
public class OrgPageApi extends BaseParam<PageParams<OrgQuery>, Page<OrgResp>> {
    @Override
    protected String method() {
        return "org.page";
    }
}