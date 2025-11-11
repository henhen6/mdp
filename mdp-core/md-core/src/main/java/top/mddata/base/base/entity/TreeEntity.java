package top.mddata.base.base.entity;

import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.util.ArrayUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mybatisflex.annotation.Column;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;


/**
 * 包括id、created_time、created_by、updated_by、updated_time、label、parent_id、sort_value 字段的表继承的树形实体
 *
 * @param <T> 主键类型
 * @param <E> 树实体类型
 * @author henhen6
 * @since 2019/05/05
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
public class TreeEntity<T, E extends TreeEntity<T, E>> extends SuperEntity<T> implements Comparable<E> {
    public static final String PARENT_ID = "parentId";
    public static final String WEIGHT = "weight";
    public static final String PARENT_ID_FIELD = "parent_id";
    public static final String WEIGHT_FIELD = "weight";
    @Schema(description = "子节点", hidden = true)
    @Column(ignore = true)
    protected List<E> children;
    /**
     * 父ID
     */
    @Schema(description = "父ID")
    private T parentId;
    /**
     * 排序
     */
    @Schema(description = "排序号")
    private Integer weight;
    @Schema(description = "父节点", hidden = true)
    @Column(ignore = true)
    @JsonIgnore
    private E parent;

    @Override
    @SuppressWarnings({"unchecked", "rawtypes", "NullableProblems"})
    public int compareTo(TreeEntity node) {
        if (node == null) {
            return 1;
        }
        final Comparable weightThis = this.getWeight();
        final Comparable weightOther = node.getWeight();
        return CompareUtil.compare(weightThis, weightOther);
    }

    /**
     * 设置父节点
     *
     * @param parent 父节点
     * @since 5.2.4
     */
    public void setParent(E parent) {
        this.parent = parent;
        if (parent != null) {
            this.setParentId(parent.getId());
        }
    }


    /**
     * 增加子节点，同时关联子节点的父节点为当前节点
     *
     * @param children 子节点列表
     * @since 5.6.7
     */
    @SafeVarargs
    public final void addChildren(E... children) {
        if (ArrayUtil.isNotEmpty(children)) {
            List<E> childrenList = this.getChildren();
            if (childrenList == null) {
                childrenList = new ArrayList<>();
                setChildren(childrenList);
            }
            for (E child : children) {
                child.setParent((E) this);
                childrenList.add(child);
            }
        }
    }
}

