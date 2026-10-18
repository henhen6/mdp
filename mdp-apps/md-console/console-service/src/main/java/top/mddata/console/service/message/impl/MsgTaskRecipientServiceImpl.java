package top.mddata.console.service.message.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.console.entity.message.MsgTaskRecipient;
import top.mddata.console.mapper.message.MsgTaskRecipientMapper;
import top.mddata.console.service.message.MsgTaskRecipientService;

import java.util.Collection;
import java.util.List;

/**
 * 任务接收人 服务层实现。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MsgTaskRecipientServiceImpl extends SuperServiceImpl<MsgTaskRecipientMapper, MsgTaskRecipient> implements MsgTaskRecipientService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByMsgTaskId(Long id) {
        ArgumentAssert.notNull(id, "任务ID不能为空");
        remove(new QueryWrapper().eq(MsgTaskRecipient::getMsgTaskId, id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByMsgTaskIds(Collection<Long> msgTaskIds) {
        ArgumentAssert.notNull(msgTaskIds, "任务ID集合不能为null");
        if (msgTaskIds.isEmpty()) {
            return;
        }
        remove(new QueryWrapper().in(MsgTaskRecipient::getMsgTaskId, msgTaskIds));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MsgTaskRecipient> listByMsgTaskId(Long id) {
        ArgumentAssert.notNull(id, "任务ID不能为空");
        return list(new QueryWrapper().eq(MsgTaskRecipient::getMsgTaskId, id));
    }
}
