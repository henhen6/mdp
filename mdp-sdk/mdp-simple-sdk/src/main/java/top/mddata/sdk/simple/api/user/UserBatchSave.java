package top.mddata.sdk.simple.api.user;

import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.simple.request.UserSaveDto;
import top.mddata.sdk.simple.response.UserVo;

import java.util.List;

public class UserBatchSave extends BaseParam<List<UserSaveDto>, UserVo> {
    @Override
    protected String method() {
        return "user.batchSave";
    }

}
