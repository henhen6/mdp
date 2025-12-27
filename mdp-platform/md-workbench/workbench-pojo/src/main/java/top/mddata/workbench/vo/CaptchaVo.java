package top.mddata.workbench.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 验证码响应参数
 *
 * @author henhen
 */
@Data
@Builder
@Schema(description = "验证码Vo")
public class CaptchaVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 验证码唯一值
     */
    @Schema(description = "验证码唯一值")
    private String key;

    /**
     * 验证码图片（Base64编码，带图片格式：data:image/gif;base64）
     */
    @Schema(description = "验证码图片（Base64编码，带图片格式：data:image/gif;base64）")
    private String img;

    /**
     * 过期时间戳
     */
    @Schema(description = "过期时间戳(单位：秒）")
    private Long expireTime;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

}
