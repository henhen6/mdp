package top.mddata.sdk.simple.api.user;

import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.simple.request.UserBatchSaveDto;
import top.mddata.sdk.simple.response.UserListVo;

public class UserBatchSave extends BaseParam<UserBatchSaveDto, UserListVo> {
    @Override
    protected String method() {
        return "user.batchSave";
    }

}
