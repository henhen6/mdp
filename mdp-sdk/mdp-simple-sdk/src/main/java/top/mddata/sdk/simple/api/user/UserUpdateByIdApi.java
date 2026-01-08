package top.mddata.sdk.simple.api.user;

import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.simple.request.user.UserUpdateDto;
import top.mddata.sdk.simple.response.user.UserResp;

/**
 * 修改用户
 * @author henhen
 * @since 2026/1/8 09:15
 */
public class UserUpdateByIdApi extends BaseParam<UserUpdateDto, UserResp> {
    @Override
    protected String method() {
        return "user.updateById";
    }

}
