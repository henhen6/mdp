package top.mddata.console.mapper.message;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.console.entity.message.InterfaceConfig;
import top.mddata.console.entity.message.base.InterfaceConfigBase;

/**
 * 接口 映射层。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:47
 */
@Repository
public interface InterfaceConfigMapper extends SuperMapper<InterfaceConfig> {

    /**
     * 统计接口总数（所有接口）。
     *
     * <p>mdc_interface_config 表没有 deleted_at 字段。</p>
     */
    @Select({
            """
            SELECT COUNT(*) AS value
              FROM
            """
            + InterfaceConfigBase.TABLE_NAME
    })
    Long countAll();
}