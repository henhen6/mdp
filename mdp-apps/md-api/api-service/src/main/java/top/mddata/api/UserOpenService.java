package top.mddata.api;

import com.gitee.sop.support.annotation.Open;
import com.mybatisflex.core.paginate.Page;
import top.mddata.api.dto.UserBatchSaveDto;
import top.mddata.api.dto.UserUpdateDto;
import top.mddata.api.query.UserQuery;
import top.mddata.api.resp.UserBatchSaveResp;
import top.mddata.api.resp.UserResp;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.common.entity.User;

/**
 * 用户数据接口
 * @author henhen
 * @since 2026/1/7 11:13
 */
public interface UserOpenService extends SuperService<User> {

    /**
     * 批量保存用户信息
     *
     * @param dto 用户信息
     * @return 保存成功后的用户信息
     * @apiNote 每次保存的用户数量不超过 500 条  <br/>
     * 用户保存成功之后，返回用户列表（含用户ID）
     */
    @Open(value = "user.batchSave", description = "批量保存用户信息")
    UserBatchSaveResp batchSave(UserBatchSaveDto dto);


    /**
     * 根据用户ID，修改用户信息
     *
     * @param dto 用户信息
     * @return 修改成功后的用户信息
     */
    @Open(value = "user.updateById", description = "根据用户ID，修改用户信息")
    UserResp updateById(UserUpdateDto dto);

    /**
     * 根据ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Open(value = "user.getById", description = "根据ID查询用户信息")
    UserResp getById(Long id);

    /**
     * 分页查询用户信息
     *
     * @param params 分页查询参数
     * @return 分页数据
     */
    @Open(value = "user.page", description = "分页查询用户信息")
    Page<UserResp> page(PageParams<UserQuery> params);
}
