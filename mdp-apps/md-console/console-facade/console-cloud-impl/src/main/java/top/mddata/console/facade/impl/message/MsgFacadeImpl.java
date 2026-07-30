package top.mddata.console.facade.impl.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.console.dto.message.MsgSendDto;
import top.mddata.console.facade.api.message.MsgApi;
import top.mddata.console.facade.message.MsgFacade;

/**
 * 消息发送服务实现类
 * @author henhen6
 * @since 2025/12/27 14:14
 */
@Service
@RequiredArgsConstructor
public class MsgFacadeImpl implements MsgFacade {
    private final MsgApi msgApi;

    @Override
    public void sendByTemplateKey(MsgSendDto data) {
        msgApi.sendByTemplateKey(data);
    }
}
