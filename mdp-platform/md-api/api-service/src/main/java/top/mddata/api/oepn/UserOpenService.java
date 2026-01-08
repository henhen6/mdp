package top.mddata.api.oepn;

import com.gitee.sop.support.annotation.Open;
import com.mybatisflex.core.paginate.Page;
import top.mddata.api.oepn.dto.UserBatchSaveDto;
import top.mddata.api.oepn.dto.UserUpdateDto;
import top.mddata.api.oepn.query.UserQuery;
import top.mddata.api.oepn.vo.UserListVo;
import top.mddata.api.oepn.vo.UserVo;
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
    @Open("user.batchSave")
    UserListVo batchSave(UserBatchSaveDto dto);


    /**
     * 根据用户ID，修改用户信息
     *
     * @param dto 用户信息
     * @return 修改成功后的用户信息
     */
    @Open("user.updateById")
    UserVo updateById(UserUpdateDto dto);

    /**
     * 根据ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Open("user.getById")
    UserVo getById(Long id);

    /**
     * 分页查询用户信息
     *
     * @param params 分页查询参数
     * @return 分页数据
     */
    @Open("user.page")
    Page<UserVo> page(PageParams<UserQuery> params);
}
