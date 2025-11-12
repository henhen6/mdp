package top.mddata.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import top.mddata.base.interfaces.BaseEnum;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 下拉选项值
 * @author henhen6
 * @date 2025年07月28日23:58:10
 */
@Data
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "value")
@Accessors(chain = true)
@AllArgsConstructor
@Builder
@Schema(title = "字典和枚举等下拉选项", description = "下拉、多选组件选项")
public class Option {
    @Schema(description = "值")
    private String value;
    @Schema(description = "名称")
    private String label;
    @Schema(description = "备注")
    private String remark;

    public Option(String value) {
        this.value = value;
    }

    /**
     * 数据转换
     * @param values 枚举数组
     * @return 操作列表
     */
    public static <T extends Serializable> List<Option> mapOptions(BaseEnum<T>[] values) {
        return Arrays.stream(values).map(item -> Option.<T>builder().value(String.valueOf(item.getCode())).label(item.getDesc()).remark(item.getDesc()).build()).toList();
    }
}
