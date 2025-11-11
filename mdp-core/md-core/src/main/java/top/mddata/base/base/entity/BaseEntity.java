package top.mddata.base.base.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 包括id、created_at、created_by字段的表继承的基础实体
 *
 * @param <T> 主键类型
 * @author henhen6
 * @since 2019/05/05
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode
public class BaseEntity<T> implements Serializable {
    public static final String ID_FIELD = "id";
    /** 创建时间 实体字段 */
    public static final String CREATED_AT = "createdAt";
    /** 创建时间 数据库字段 */
    public static final String CREATED_AT_FIELD = "created_at";
    /** 创建人ID 实体字段 */
    public static final String CREATED_BY = "createdBy";
    /** 创建人ID 数据库字段 */
    public static final String CREATED_BY_FIELD = "created_by";
    /** 删除人 实体字段 */
    public static final String DELETED_BY = "deletedBy";
    /** 删除人 数据库字段 */
    public static final String DELETED_BY_FIELD = "deleted_by";
    /** 删除标记 实体字段 */
    public static final String DELETED_AT = "deletedAt";
    /** 删除标记 数据库字段 */
    public static final String DELETED_AT_FIELD = "deleted_at";
    /** 创建人所在公司 实体字段 */
    public static final String CREATED_BY_COMPANY = "createdByCompany";
    /** 创建人所在公司 数据库字段 */
    public static final String CREATED_BY_COMPANY_FIELD = "created_by_company";
    /** 创建人所在部门 实体字段 */
    public static final String CREATED_BY_DEPT = "createdByDept";
    /** 创建人所在部门 数据库字段 */
    public static final String CREATED_BY_DEPT_FIELD = "created_by_dept";

    @Serial
    private static final long serialVersionUID = -4603650115461757622L;

    @Schema(description = "主键")
    @NotNull(message = "id不能为空", groups = Update.class)
    @Id(keyType = KeyType.Generator, value = "uid")
    private T id;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "创建人ID")
    private T createdBy;

    /**
     * 保存验证组
     */
    public interface Save extends Default {

    }

    /**
     * 更新验证组
     */
    public interface Update extends Default {

    }
}
