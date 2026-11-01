package top.mddata.console.facade.impl.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.console.dto.message.MsgSendDto;
import top.mddata.console.facade.message.MsgFacade;
import top.mddata.console.service.message.MsgTaskService;

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
    public Long sendByTemplateKey(MsgSendDto data) {
        return msgTaskService.sendByTemplateKey(data);
    }
}
