package top.mddata.console.message.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 接口统计实体类。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class InterfaceStatBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_interface_stat";

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接口ID;
     * #extend_interface
     */
    private Long interfaceConfigId;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 成功次数
     */
    private Integer successCount;

    /**
     * 失败次数
     */
    private Integer failCount;

    /**
     * 最后执行时间
     */
    private LocalDateTime lastExecAt;

}
