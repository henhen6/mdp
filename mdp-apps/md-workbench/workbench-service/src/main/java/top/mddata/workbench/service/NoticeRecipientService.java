package top.mddata.workbench.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.workbench.entity.NoticeRecipient;

/**
 * 通知接收人 服务层。
 *
 * @author henhen6
 * @since 2025-12-26 09:55:35
 */
public interface NoticeRecipientService extends SuperService<NoticeRecipient> {

    /**
     * 统计指定用户的站内通知未读数。
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    Long countUnreadByUserId(Long userId);
}
