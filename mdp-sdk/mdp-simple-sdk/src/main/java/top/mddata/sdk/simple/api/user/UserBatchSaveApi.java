package top.mddata.sdk.simple.api.user;

import top.mddata.sdk.core.param.BaseParam;
import top.mddata.sdk.simple.request.user.UserBatchSaveDto;
import top.mddata.sdk.simple.response.user.UserBatchSaveResp;

/**
 * 批量保存用户
 * @author henhen
 */
public class UserBatchSaveApi extends BaseParam<UserBatchSaveDto, UserBatchSaveResp> {
    @Override
    protected String method() {
        return "user.batchSave";
    }

}
