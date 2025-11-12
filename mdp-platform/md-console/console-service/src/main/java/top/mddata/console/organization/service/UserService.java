package top.mddata.console.organization.service;

import com.mybatisflex.core.paginate.Page;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.common.entity.User;
import top.mddata.console.organization.dto.UserResetPasswordDto;
import top.mddata.console.organization.query.UserQuery;
import top.mddata.console.organization.vo.UserVo;

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
     * @return
     */
    Page<UserVo> page(PageParams<UserQuery> params);

    /**
     * 解锁用户
     * @param id 用户id
     * @return 是否成功
     */
    Boolean unlock(Long id);

    /**
     * 重置密码
     * @param data 参数
     * @return
     */
    Boolean resetPassword(UserResetPasswordDto data);
}
