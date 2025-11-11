package top.mddata.base.base.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 包括id、created_time、created_by、updated_by、updated_time字段的表继承的基础实体
 *
 * @param <T> 主键类型
 * @author henhen6
 * @since 2019/05/05
 */
@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SuperEntity<T> extends BaseEntity<T> {

    public static final String UPDATED_AT = "updatedAt";
    public static final String UPDATED_BY = "updatedBy";
    public static final String UPDATED_AT_FIELD = "updated_at";
    public static final String UPDATED_BY_FIELD = "updated_by";
    @Serial
    private static final long serialVersionUID = 5169873634279173683L;

    @Schema(description = "最后修改时间")
    private LocalDateTime updatedAt;

    @Schema(description = "最后修改人ID")
    private T updatedBy;

    public SuperEntity(T id, LocalDateTime createdAt, T createdBy, LocalDateTime updatedAt, T updatedBy) {
        super(id, createdAt, createdBy);
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public SuperEntity() {
    }

}
