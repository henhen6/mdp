package top.mddata.console.organization.service;

import com.mybatisflex.core.paginate.Page;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.common.entity.User;
import top.mddata.console.organization.dto.UserResetPasswordDto;
import top.mddata.console.organization.query.UserQuery;
import top.mddata.console.organization.vo.UserVo;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 15:44:52
 */
public interface UserService extends SuperService<User> {
    /**
     * 分页查询用户
     * @param params 分页参数
     * @return 分页结果
     */
    Page<UserVo> page(PageParams<UserQuery> params);

    /**
     * 解锁用户
     * @param id 用户id
     * @return true=成功，false=失败
     */
    Boolean unlock(Long id);

    /**
     * 重置密码
     * @param data 参数
     * @return true=成功，false=失败
     */
    Boolean resetPassword(UserResetPasswordDto data);

    /**
     * 检查用户名
     * @param username 用户名
     * @param id 用户id
     * @return true=存在，false=不存在
     */
    Boolean checkUsername(String username, Long id);

    /**
     * 检查手机号
     * @param phone 手机号
     * @param id 用户id
     * @return true=存在，false=不存在
     */
    Boolean checkPhone(String phone, Long id);

    /**
     * 检查邮箱
     * @param email 邮箱
     * @param id 用户id
     * @return true=存在，false=不存在
     */
    Boolean checkEmail(String email, Long id);

    /**
     * 根据角色id列表查询用户
     * @param roleIds 角色id列表
     * @return 用户列表
     */
    List<User> listByRoleIds(List<Long> roleIds);

    /**
     * 根据部门id列表查询用户
     * @param deptIds 部门id列表
     * @return 用户列表
     */
    List<User> listByDeptIds(List<Long> deptIds);
}
