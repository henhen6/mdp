package top.mddata.sdk.simple.api.user;


import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.simple.request.user.UserGetByIdDto;
import top.mddata.sdk.simple.response.user.UserResp;

/**
 * 获取用户信息
 * @author henhen
 */
public class UserGetByIdApi extends BaseParam<UserGetByIdDto, UserResp> {
    @Override
    protected String method() {
        return "user.getById";
    }
}