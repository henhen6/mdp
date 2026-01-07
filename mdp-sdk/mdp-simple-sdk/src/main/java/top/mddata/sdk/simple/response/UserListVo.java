package top.mddata.sdk.simple.response;


import java.util.List;

public class UserListVo {
    /**
     * 新增数据
     */
    
    private List<UserVo> saveList;
    /**
     * 修改数据
     */
    
    private List<UserVo> updateList;
    /**
     * 重复数据
     */
    
    private List<UserVo> existList;

    public List<UserVo> getSaveList() {
        return saveList;
    }

    public UserListVo setSaveList(List<UserVo> saveList) {
        this.saveList = saveList;
        return this;
    }

    public List<UserVo> getUpdateList() {
        return updateList;
    }

    public UserListVo setUpdateList(List<UserVo> updateList) {
        this.updateList = updateList;
        return this;
    }

    public List<UserVo> getExistList() {
        return existList;
    }

    public UserListVo setExistList(List<UserVo> existList) {
        this.existList = existList;
        return this;
    }
}
