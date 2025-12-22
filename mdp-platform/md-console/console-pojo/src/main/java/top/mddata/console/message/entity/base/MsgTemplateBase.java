package top.mddata.console.message.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 消息模板实体类。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class MsgTemplateBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_msg_template";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接口ID
     */
    private Long interfaceConfigId;

    /**
     * 消息类型
     * [1-站内信 2-短信 3-邮件 ]
     */
    private Integer msgType;

    /**
     * 模板标识
     */
    private String key;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 状态
     */
    private Boolean state;

    /**
     * 模板编码
     * 第三方模板编码（如，消息类型为短信时，第三方短信商的模版id）
     */
    private String smsTemplateId;

    /**
     * 签名
     */
    private String smsSign;

    /**
     * 标题
     */
    private String title;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 脚本
     */
    private String script;

    /**
     * 参数
     */
    private String param;


    /**
     * 备注
     */
    private String remarks;

}
