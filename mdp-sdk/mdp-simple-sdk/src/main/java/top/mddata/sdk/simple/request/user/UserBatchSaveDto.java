package top.mddata.sdk.simple.request.user;

import java.io.Serializable;
import java.util.List;


/**
 * 用户新增或修改
 *
 * @author henhen
 * @since 2025-10-19 09:45:12
 */
public class UserBatchSaveDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<UserSaveDto> list;

    public List<UserSaveDto> getList() {
        return list;
    }

    public UserBatchSaveDto setList(List<UserSaveDto> list) {
        this.list = list;
        return this;
    }
}
