package top.mddata.sdk.simple.api.user;

import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.core.request.PageParams;
import top.mddata.sdk.core.response.Page;
import top.mddata.sdk.simple.request.user.UserQuery;
import top.mddata.sdk.simple.response.user.UserResp;

/**
 * 分页查询用户信息
 * <p>
 * user.page
 *
 * @author henhen
 */
public class UserPageApi extends BaseParam<PageParams<UserQuery>, Page<UserResp>> {
    @Override
    protected String method() {
        return "user.page";
    }
}