package top.mddata.console.message.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import top.mddata.console.message.event.MsgSendEvent;
import top.mddata.console.message.event.dto.MsgSendEventDto;
import top.mddata.console.message.strategy.MsgTaskContext;

/**
 * 消息发送事件监听
 * 目的： 解耦
 *
 * @author henhen
 * @date 2020年03月18日17:39:59
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MsgSendListener {
    private final MsgTaskContext msgTaskContext;

    /**
     * 消息任务异步发送处理
     *
     * 等事务处理完毕后才会触发此事件
     * @param event
     */
    @Async
    @TransactionalEventListener({MsgSendEvent.class})
    public void handleMsgSend(MsgSendEvent event) {
        MsgSendEventDto dto = (MsgSendEventDto) event.getSource();
        dto.write();
        msgTaskContext.execSend(dto.getMsgTaskId());
    }

}
