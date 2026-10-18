package top.mddata.console.service.message;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.entity.message.MsgTaskRecipient;

import java.util.Collection;
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

    /**
     * 根据任务ID批量删除
     * @param msgTaskIds 任务ID集合，空集合视为无操作
     */
    void removeByMsgTaskIds(Collection<Long> msgTaskIds);
}
