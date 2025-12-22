package top.mddata.console.message.strategy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.console.message.entity.MsgTask;
import top.mddata.console.message.entity.MsgTaskRecipient;
import top.mddata.console.message.entity.MsgTemplate;

import java.util.List;
import java.util.Map;

/**
 * 消息任务执行参数
 *
 * @author henhen
 * @version v1.0
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class MsgTaskParam {
    /** 消息任务 */
    private MsgTask msgTask;
    /** 消息接收人 */
    private List<MsgTaskRecipient> recipientList;
    /** 采用的消息模板 */
    private MsgTemplate msgTemplate;
    /** 接口需要使用的动态参数 */
    private Map<String, String> propertyParam;

}
