package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.mddata.workbench.enumeration.TwoFactorChannelEnum;
import top.mddata.workbench.enumeration.TwoFactorStrategyEnum;

/**
 * 双因素认证状态 Vo（安全设置页展示）
 *
 * @author tangyh
 */
@Data
@Schema(title = "TwoFactorStatusVo", description = "双因素认证状态")
public class TwoFactorStatusVo {

    @Schema(description = "是否已开通")
    private Boolean enabled;

    @Schema(description = "验证渠道")
    private TwoFactorChannelEnum channel;

    @Schema(description = "全局策略")
    private TwoFactorStrategyEnum strategy;

    @Schema(description = "脱敏接收目标（手机号/邮箱）")
    private String maskedTarget;
}
