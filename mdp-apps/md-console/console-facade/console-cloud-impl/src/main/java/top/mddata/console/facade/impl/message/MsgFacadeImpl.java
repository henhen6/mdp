package top.mddata.console.facade.impl.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.base.exception.ArgumentException;
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
    public Long sendByTemplateKey(MsgSendDto data) {
        R<Long> result = msgApi.sendByTemplateKey(data);
        if (result == null || !result.getIsSuccess()) {
            throw new ArgumentException("发送消息失败: {}", result != null ? result.getMsg() : "Feign调用失败");
        }
        return result.getData();
    }
}
