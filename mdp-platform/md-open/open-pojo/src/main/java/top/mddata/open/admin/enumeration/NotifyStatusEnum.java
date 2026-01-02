package top.mddata.open.admin.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import top.mddata.base.interfaces.BaseEnum;


/**
 * 通知状态
 * 0-待发送 1-发送成功,2-发送失败,3-重试结束 4-手动结束
 */
@AllArgsConstructor
@Getter
@Schema(description = "通知执行状态-枚举")
public enum NotifyStatusEnum implements BaseEnum<String> {

    WAIT_SEND("0", "待执行"),
    SEND_SUCCESS("1", "执行成功"),
    SEND_FAIL("2", "执行失败"),
    RETRY_OVER("3", "重试结束"),
    END("4", "手动结束");
    @Schema(description = "状态")
    private final String code;
    @Schema(description = "描述")
    private final String desc;

}
