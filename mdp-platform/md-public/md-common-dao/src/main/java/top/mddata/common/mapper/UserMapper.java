package top.mddata.common.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.common.entity.User;
import top.mddata.common.entity.base.UserBase;

import java.time.LocalDateTime;

/**
 * 用户 映射层。
 *
 * @author henhen6
 * @since 2025-11-12 15:44:52
 */
@Repository
public interface UserMapper extends SuperMapper<User> {
    /**
     * 重置 密码错误次数
     *
     * @param id  用户id
     * @param now 当前时间
     */
    @Update({
            """
                    update 
                    """
            + UserBase.TABLE_NAME +
            """
                        set pw_error_num       = 0, pw_error_last_time = null, last_login_time          = #{now, jdbcType=TIMESTAMP}
                     where id = #{id, jdbcType=BIGINT}
                    """
    })
    void resetPwErrorNum(@Param("id") Long id, @Param("now") LocalDateTime now);

    /**
     * 递增 密码错误次数
     *
     * @param id  用户id
     * @param now 当前时间
     */
    @Update({
            """
                    update 
                    """
            + UserBase.TABLE_NAME +
            """
                        set pw_error_num       = pw_error_num + 1, pw_error_last_time = #{now, jdbcType=TIMESTAMP}
                        where id = #{id, jdbcType=BIGINT}
                    """})
    void incrPwErrorNumById(@Param("id") Long id, @Param("now") LocalDateTime now);
}
