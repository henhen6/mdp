package top.mddata.console.message.event.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.mddata.common.vo.BaseEventVO;

/**
 * 消息发送事件DTO
 * @author henhen
 * @date 2025年12月21日23:03:58
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MsgSendEventDto extends BaseEventVO {
    Long msgTaskId;
}
