package top.mddata.console.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 根据模版编码发送消息任务
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "短信发送Dto")
public abstract class MsgSendSmsDto extends MsgSendDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "接收人手机号")
    private List<String> recipientList;


}
