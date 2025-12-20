package top.mddata.console.message.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.ExtraParams;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息模板 Query类（查询方法入参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Schema(description = "消息模板Query")
public class MsgTemplateQuery extends ExtraParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    @Schema(description = "模板ID")
    private Long id;

    /**
     * 接口ID
     */
    @Schema(description = "接口ID")
    private Long interfaceConfigId;

    /**
     * 消息类型
     * [1-站内信 2-短信 3-邮件 ]
     */
    @Schema(description = "消息类型")
    private Integer msgType;

    /**
     * 模板标识
     */
    @Schema(description = "模板标识")
    private String key;

    /**
     * 模板名称
     */
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
    @Schema(description = "模板编码")
    private String smsTemplateId;

    /**
     * 签名
     */
    @Schema(description = "签名")
    private String smsSign;

    /**
     * 标题
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 模板内容
     */
    @Schema(description = "模板内容")
    private String content;

    /**
     * 脚本
     */
    @Schema(description = "脚本")
    private String script;

    /**
     * 标题参数
     */
    @Schema(description = "标题参数")
    private String titleParam;

    /**
     * 模板参数
     * 从模板内容中自动解析出来的参数
     */
    @Schema(description = "模板参数")
    private String contentParam;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remarks;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Long createdBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 最后修改人
     */
    @Schema(description = "最后修改人")
    private Long updatedBy;

    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间")
    private LocalDateTime updatedAt;

}
