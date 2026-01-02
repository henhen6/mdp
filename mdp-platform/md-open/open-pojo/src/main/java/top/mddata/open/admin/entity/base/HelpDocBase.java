package top.mddata.open.admin.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 帮助文档实体类。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class HelpDocBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_help_doc";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文档名称
     */
    private String title;

    /**
     * 排序
     */
    private Integer weight;

    /**
     * 状态
     * 1：启用，0：禁用
     */
    private Boolean state;

    /**
     * 内容
     */
    private String content;

    /**
     * 内容类型
     * 1-Markdown,2-富文本
     */
    private Integer contentType;

    /**
     * 父级id
     */
    private Long parentId;

}
