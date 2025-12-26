package top.mddata.workbench.facade;

import top.mddata.workbench.entity.Notice;
import top.mddata.workbench.entity.NoticeRecipient;

import java.util.List;

/**
 * 通知 接口
 * @author henhen6
 * @since 2025/7/27 00:39
 */
public interface NoticeFacade {

    /**
     * 批量保存 站内通知接收人
     * @param recipientList 站内通知接收人
     */
    void saveBatchNoticeRecipient(List<NoticeRecipient> recipientList);

    /**
     * 保存 通知
     * @param notice 通知
     */
    void save(Notice notice);
}
