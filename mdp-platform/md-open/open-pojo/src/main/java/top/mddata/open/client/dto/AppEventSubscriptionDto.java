package top.mddata.open.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 应用秘钥修改
 * @author henhen6
 * @since 2025/11/20 20:48
 */
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应用修改自己的事件订阅")
public class AppEventSubscriptionDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * appId
     */
    @NotNull(message = "请填写id")
    @Schema(description = "id")
    private Long id;
    /**
     * 通知状态
     */
    @NotNull(message = "请填写通知状态")
    @Schema(description = "通知状态")
    private Boolean notifyState;


    /**
     * 通知地址
     */
    @Size(max = 255, message = "通知地址长度不能超过255")
    @Schema(description = "通知地址")
    private String notifyUrl;


    /**
     * 加密类型
     * [0-不加密 1-aes加密 2-sm4加密]
     */
    @NotNull(message = "请填写加密类型")
    @Schema(description = "加密类型")
    private Integer notifyEncryptionType;

    @Schema(description = "订阅的事件")
    private List<Long> eventTypeIdList;
}
