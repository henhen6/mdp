package top.mddata.workbench.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.workbench.entity.base.NoticeBase;

import java.time.LocalDateTime;

/**
 * 站内通知实体类。
 * DO类：数据对象，可以在关联查询时，再次添加字段，重新生成代码时，忽略此文件。
 *
 * @author henhen6
 * @since 2025-12-26 09:47:55
 */
@FieldNameConstants
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(NoticeBase.TABLE_NAME)
public class Notice extends NoticeBase {
    /**
     * 是否已读
     */
    private Boolean read;

    /**
     * 已读时间
     */
    private LocalDateTime readTime;
}
