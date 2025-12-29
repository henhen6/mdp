package top.mddata.console.message.entity.base;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.core.handler.Fastjson2TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import top.mddata.base.base.entity.SuperEntity;
import top.mddata.console.message.dto.InterfaceConfigJsonDto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 接口实体类。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@FieldNameConstants
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class InterfaceConfigBase extends SuperEntity<Long> implements Serializable {
    /** 表名称 */
    public static final String TABLE_NAME = "mdc_interface_config";

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 接口标识
     */
    private String key;
    /**
     * 接口名称
     */
    private String name;

    /**
     * 执行方式
     * [1-实现类 2-脚本 3-magic-api]
     */
    private Integer execMode;

    /**
     * 实现脚本
     */
    private String script;

    /**
     * 实现类
     */
    private String implClass;

    /**
     * 实现ID
     */
    private String magicApiId;

    /**
     * 配置参数
     * (JSON存储：AppId, SecretKey等)
     */
    @Column(typeHandler = Fastjson2TypeHandler.class)
    private List<InterfaceConfigJsonDto> configJson;

    /**
     * 状态
     */
    private Boolean state;

}
