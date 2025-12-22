package top.mddata.console.message.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.message.entity.MsgTaskRecipient;

import java.util.List;

/**
 * 任务接收人 服务层。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
public interface MsgTaskRecipientService extends SuperService<MsgTaskRecipient> {
    /**
     * 根据任务ID查询接收人
     * @param id 消息任务id
     * @return 接收人
     */
    List<MsgTaskRecipient> listByMsgTaskId(Long id);

    /**
     * 根据任务ID删除
     * @param id 任务ID
     */
    void removeByMsgTaskId(Long id);
}
