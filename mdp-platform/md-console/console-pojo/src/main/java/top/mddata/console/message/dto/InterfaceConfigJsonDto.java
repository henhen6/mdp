package top.mddata.console.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.io.Serial;
import java.io.Serializable;

/**
 * 接口 DTO（写入方法入参）。
 *
 * @author henhen6
 * @since 2025-12-21 00:30:09
 */
@Accessors(chain = true)
@Data
@FieldNameConstants
@Schema(description = "接口参数Dto")
public class InterfaceConfigJsonDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String key;
    private String value;
    private String remark;

}
