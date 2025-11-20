package top.mddata.open.admin.vo;

import lombok.Data;
import top.mddata.open.admin.TreeNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author henhen6
 */
@Data
public class TornaDocParamVo implements TreeNode<TornaDocParamVo, Long> {

    private Long id;

    /**
     * 字段名称
     */

    private String name;

    /**
     * 字段类型
     */

    private String type;

    /**
     * 是否必须，1：是，0：否
     */

    private Byte required;

    /**
     * 最大长度
     */

    private String maxLength;

    /**
     * 示例值
     */
    private String example;

    /**
     * 描述
     */

    private String description;


    private Long enumId;

    /**
     * doc_info.id
     */

    private Long docId;

    /**
     * 父节点
     */

    private Long parentId;

    /**
     * 0：header, 1：请求参数，2：返回参数，3：错误码
     */
    private Byte style;

    /**
     * 新增操作方式，0：人工操作，1：开放平台推送
     */
    private Byte createMode;

    /**
     * 修改操作方式，0：人工操作，1：开放平台推送
     */
    private Byte modifyMode;

    /**
     * 创建人
     */
    private String creatorName;

    /**
     * 修改人
     */
    private String modifierName;

    /**
     * 排序
     */
    private Integer orderIndex;

    private Byte isDeleted;

    /**
     * 数据库字段：gmt_create
     */
    private LocalDateTime gmtCreate;

    /**
     * 数据库字段：gmt_modified
     */
    private LocalDateTime gmtModified;

    private boolean global;

    private List<TornaDocParamVo> children;


    public boolean getRequire() {
        return Objects.equals(this.required, 1);
    }

    @Override
    public Long takeId() {
        return id;
    }

    @Override
    public Long takeParentId() {
        return parentId;
    }

}
