package top.mddata.console.dto.coderule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 编码规则明细配置（各类别字段扁平承载，按 ruleType 取用对应字段）。
 * 字段名统一 camelCase，与 config_json 列内容一一对应。
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "编码规则明细配置")
public class CodeRuleItemConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // ===== 10-常量 =====
    /** 固定值 */
    @Schema(description = "固定值")
    private String value;

    // ===== 20-日期 =====
    /** 日期格式（Java DateTimeFormatter 占位符，如 yyyyMMddHHmmss） */
    @Schema(description = "日期格式")
    private String pattern;

    // ===== 30-变量 / 90-父单号截取 =====
    /** 变量名（调用方 map 的 key） */
    @Schema(description = "变量名")
    private String varName;
    /** 变量类型 [MAPPING-映射 REPLACE-替换] */
    @Schema(description = "变量类型")
    private String varType;
    /** 映射表（varType=MAPPING 时使用；key 无映射时显示 key 本身兜底） */
    @Schema(description = "映射表")
    private Map<String, String> mappings;

    // ===== 40-流水号 =====
    /** 初始值 */
    @Schema(description = "初始值")
    private Long initValue;
    /** 递增值 */
    @Schema(description = "递增值")
    private Long step;
    /** 长度（流水号=固定宽度；随机码=精确长度；GUID=截取长度；父单号截取=INDEX 模式截取长度） */
    @Schema(description = "长度")
    private Integer length;
    /** 补位字符（默认 0） */
    @Schema(description = "补位字符")
    private String padChar;
    /** 归零规则 [NEVER YEAR MONTH WEEK DAY] */
    @Schema(description = "归零规则")
    private String resetRule;
    /** 超长策略 [ERROR-报错 GROW-超长递增] */
    @Schema(description = "超长策略")
    private String overflowStrategy;
    /** 周期偏移量（只作用于 periodKey 计算；0=自然周期） */
    @Schema(description = "周期偏移量")
    private Integer cycleOffset;
    /** 偏移单位 [HOUR DAY] */
    @Schema(description = "偏移单位")
    private String offsetUnit;

    // ===== 50-随机码 =====
    /** 字符集 [DIGIT UPPER LOWER DIGIT_UPPER DIGIT_LOWER UPPER_LOWER ALL] */
    @Schema(description = "字符集")
    private String charset;

    // ===== 60-校验位 =====
    /** 算法 [LUHN LUHN_MOD_N] */
    @Schema(description = "校验位算法")
    private String algorithm;

    // ===== 70-GUID/雪花 =====
    /** 类型 [GUID SNOWFLAKE] */
    @Schema(description = "GUID类型")
    private String guidType;

    // ===== 80-组织编码 =====
    /** 来源 [DEPT-当前用户部门编码 COMPANY-当前用户机构编码] */
    @Schema(description = "组织编码来源")
    private String source;

    // ===== 90-父单号截取 =====
    /** 截取方式 [INDEX-按下标 SEPARATOR-按分隔符] */
    @Schema(description = "截取方式")
    private String cutType;
    /** 起始下标（cutType=INDEX） */
    @Schema(description = "起始下标")
    private Integer startIndex;
    /** 分隔符（cutType=SEPARATOR） */
    @Schema(description = "分隔符")
    private String separator;
    /** 第几个分隔符（支持负数从后往前数，-1=最后一个；0 无意义报错） */
    @Schema(description = "第几个分隔符")
    private Integer splitPosition;
}
