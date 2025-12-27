package top.mddata.console.message.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.console.message.dto.MsgSendDto;
import top.mddata.console.message.facade.MsgFacade;
import top.mddata.console.message.service.MsgTaskService;

/**
 * 消息发送服务实现类
 * @author henhen6
 * @since 2025/12/27 14:14
 */
@Service
@RequiredArgsConstructor
public class MsgFacadeImpl implements MsgFacade {
    private final MsgTaskService msgTaskService;

    @Override
    public void sendByTemplateKey(MsgSendDto data) {
        msgTaskService.sendByTemplateKey(data);
    }
}
