package top.mddata.workbench.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 携带二次验证票据的入参
 *
 * @author tangyh
 */
@Data
@Schema(title = "SocialTicketDto", description = "二次验证票据入参")
public class SocialTicketDto {

    /**
     * 二次验证票据
     */
    @Schema(description = "二次验证票据")
    @NotBlank(message = "二次验证票据不能为空")
    private String reauthTicket;
}