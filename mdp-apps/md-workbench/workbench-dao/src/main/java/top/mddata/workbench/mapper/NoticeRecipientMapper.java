package top.mddata.workbench.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.workbench.entity.NoticeRecipient;

/**
 * 通知接收人 映射层。
 *
 * @author henhen6
 * @since 2025-12-26 09:55:35
 */
@Repository
public interface NoticeRecipientMapper extends SuperMapper<NoticeRecipient> {

    /**
     * 统计当前用户的站内通知未读数。
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    @Select({
            """
            SELECT COUNT(*)
              FROM mdc_notice_recipient
             WHERE user_id = #{userId}
               AND `read` = 0
            """
    })
    Long countUnreadByUserId(@Param("userId") Long userId);
}
