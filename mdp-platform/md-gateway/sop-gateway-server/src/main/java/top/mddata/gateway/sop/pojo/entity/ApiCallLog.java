package top.mddata.gateway.sop.pojo.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.mddata.gateway.sop.pojo.entity.base.ApiCallLogBase;

/**
 * 调用日志实体类。
 * DO类：数据对象，可以在关联查询时，再次添加字段，重新生成代码时，忽略此文件。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Table(ApiCallLogBase.TABLE_NAME)
public class ApiCallLog extends ApiCallLogBase {
}
