package top.mddata.sdk.simple.response.user;


import java.util.List;

public class UserBatchSaveResp {
    /**
     * 新增数据
     */
    private List<UserResp> saveList;
    /**
     * 修改数据
     */

    private List<UserResp> updateList;
    /**
     * 重复数据
     */
    private List<UserResp> existList;

    public List<UserResp> getSaveList() {
        return saveList;
    }

    public UserBatchSaveResp setSaveList(List<UserResp> saveList) {
        this.saveList = saveList;
        return this;
    }

    public List<UserResp> getUpdateList() {
        return updateList;
    }

    public UserBatchSaveResp setUpdateList(List<UserResp> updateList) {
        this.updateList = updateList;
        return this;
    }

    public List<UserResp> getExistList() {
        return existList;
    }

    public UserBatchSaveResp setExistList(List<UserResp> existList) {
        this.existList = existList;
        return this;
    }
}
