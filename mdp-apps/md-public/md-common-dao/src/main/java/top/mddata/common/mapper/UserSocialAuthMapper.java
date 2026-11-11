package top.mddata.common.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.common.entity.UserSocialAuth;
import top.mddata.common.entity.base.UserSocialAuthBase;

import java.time.LocalDateTime;

/**
 * 用户第三方身份绑定 映射层。
 *
 * @author tangyh
 */
@Repository
public interface UserSocialAuthMapper extends SuperMapper<UserSocialAuth> {

    /**
     * 转移绑定归属用户。
     * 必须同时匹配绑定记录ID和源用户，防止并发操作把绑定改到错误的用户。
     *
     * @param id           绑定记录ID
     * @param sourceUserId 源用户ID
     * @param targetUserId 目标用户ID
     * @param operatorId   操作人ID
     * @param now          当前时间
     * @return 受影响行数，为 1 表示转移成功
     */
    @Update("""
            UPDATE """ + UserSocialAuthBase.TABLE_NAME + """
               SET user_id = #{targetUserId}, bound_at = #{now},
                   updated_at = #{now}, updated_by = #{operatorId}
             WHERE id = #{id} AND user_id = #{sourceUserId} AND deleted_at = 0
            """)
    int transferUser(@Param("id") Long id, @Param("sourceUserId") Long sourceUserId,
                     @Param("targetUserId") Long targetUserId,
                     @Param("operatorId") Long operatorId, @Param("now") LocalDateTime now);

    /**
     * 按绑定记录ID和当前用户条件逻辑删除。
     *
     * @param id        绑定记录ID
     * @param userId    当前用户ID
     * @param operatorId 操作人ID
     * @param deletedAt 删除时间戳
     * @param now       当前时间
     * @return 受影响行数，为 1 表示解绑成功
     */
    @Update("""
            UPDATE """ + UserSocialAuthBase.TABLE_NAME + """
               SET deleted_at = #{deletedAt}, deleted_by = #{operatorId},
                   updated_at = #{now}, updated_by = #{operatorId}
             WHERE id = #{id} AND user_id = #{userId} AND deleted_at = 0
            """)
    int logicalDeleteByUser(@Param("id") Long id, @Param("userId") Long userId,
                            @Param("operatorId") Long operatorId,
                            @Param("deletedAt") Long deletedAt, @Param("now") LocalDateTime now);
}
