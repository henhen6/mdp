package top.mddata.open.entity.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文档信息实体类。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DocInfoBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdo_doc_info";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属分组
     */
    private Long docGroupId;

    /**
     * 文档标题
     */
    private String docTitle;

    /**
     * 文档编码
     */
    private String docCode;

    /**
     * 文档类型
     * [1-dubbo 2-富文本 3-Markdown]
     */
    private Integer docType;

    /**
     * 来源类型
     * [1-torna 2-自建]
     */
    private Integer sourceType;

    /**
     * 文档版本号
     */
    private String docVersion;

    /**
     * 文档名称
     */
    private String docName;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否分类
     */
    private Integer folder;

    /**
     * 发布状态
     * [0-未发布 1-已发布]
     */
    private Integer publish;

    /**
     * 父文档
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer weight;

}
