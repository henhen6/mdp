package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 二次验证票据结果
 *
 * @author tangyh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SocialReauthTicketVo", description = "二次验证票据结果")
public class SocialReauthTicketVo {

    /**
     * 二次验证票据
     */
    @Schema(description = "二次验证票据")
    private String reauthTicket;
}