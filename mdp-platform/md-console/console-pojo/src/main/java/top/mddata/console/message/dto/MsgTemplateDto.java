package top.mddata.console.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.BaseEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 消息模板 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "消息模板Dto")
public class MsgTemplateDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    @NotNull(message = "请填写模板ID", groups = BaseEntity.Update.class)
    @Schema(description = "模板ID")
    private Long id;

    /**
     * 接口ID
     */
    @NotNull(message = "请填写接口ID")
    @Schema(description = "接口ID")
    private Long interfaceConfigId;

    /**
     * 模板标识
     */
    @NotEmpty(message = "请填写模板标识")
    @Size(max = 255, message = "模板标识长度不能超过{max}")
    @Schema(description = "模板标识")
    private String key;

    /**
     * 模板名称
     */
    @Size(max = 255, message = "模板名称长度不能超过{max}")
    @Schema(description = "模板名称")
    private String name;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private Boolean state;

    /**
     * 模板编码
     * 第三方模板编码（如，消息类型为短信时，第三方短信商的模版id）
     */
    @Size(max = 255, message = "模板编码长度不能超过{max}")
    @Schema(description = "模板编码")
    private String smsTemplateId;

    /**
     * 签名
     */
    @Size(max = 255, message = "签名长度不能超过{max}")
    @Schema(description = "签名")
    private String smsSign;

    /**
     * 标题
     */
    @Size(max = 255, message = "标题长度不能超过{max}")
    @Schema(description = "标题")
    private String title;

    /**
     * 模板内容
     */
    @NotEmpty(message = "请填写模板内容")
    @Size(max = 536870911, message = "模板内容长度不能超过{max}")
    @Schema(description = "模板内容")
    private String content;

    /**
     * 脚本
     */
    @Size(max = 536870911, message = "脚本长度不能超过{max}")
    @Schema(description = "脚本")
    private String script;

    /**
     * 参数
     */
    @Size(max = 16383, message = "参数长度不能超过{max}")
    @Schema(description = "参数")
    private String param;


    /**
     * 备注
     */
    @Size(max = 255, message = "备注长度不能超过{max}")
    @Schema(description = "备注")
    private String remarks;

}
