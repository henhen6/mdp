package top.mddata.codegen.config.front;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.mddata.codegen.config.BaseConfig;

/**
 * 前端列表配置
 * @author henhen6
 * @since 2024/7/9 08:34
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class ListConfig extends BaseConfig {
    /** 是否生成该字段 */
    private Boolean show;
    /** 是否隐藏字段 */
    private Boolean hidden;
    /** 列宽 */
    private String width;
    /**
     * 自适应宽度
     */
    private Boolean autoWidth;
    /** 对齐方式 */
    private String align;
    /**
     * 顺序 升序
     */
    private int sequence;
}
