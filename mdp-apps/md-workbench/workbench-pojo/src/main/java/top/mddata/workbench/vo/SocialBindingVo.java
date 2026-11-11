package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 第三方账号绑定状态
 *
 * @author tangyh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SocialBindingVo", description = "第三方账号绑定状态")
public class SocialBindingVo {

    /**
     * 平台编码
     */
    @Schema(description = "平台编码")
    private String provider;

    /**
     * 是否已绑定
     */
    @Schema(description = "是否已绑定")
    private boolean bound;

    /**
     * 第三方昵称
     */
    @Schema(description = "第三方昵称")
    private String nickname;

    /**
     * 绑定时间
     */
    @Schema(description = "绑定时间")
    private LocalDateTime boundAt;
}