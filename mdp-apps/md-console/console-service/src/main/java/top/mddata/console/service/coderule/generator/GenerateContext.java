package top.mddata.console.service.coderule.generator;

import lombok.Getter;

import java.util.Map;

/**
 * 单号生成上下文（一次调用固定生成时刻与入参）。
 */
@Getter
public class GenerateContext {

    /** 编码标识 */
    private final String ruleCode;
    /** 调用方传入变量 */
    private final Map<String, String> params;
    /** 生成时刻（一次调用内固定，保证批量时各段日期一致） */
    private final java.time.LocalDateTime bizTime;
    /** 上下文用户ID（组织编码类别用，可为空） */
    private final Long userId;
    /** 已生成前文（校验位类别基于前文计算） */
    private final StringBuilder prefix = new StringBuilder();
    /** 当前正在生成的明细ID（引擎每段生成前设置，用于区分多流水号明细） */
    private Long currentItemId;
    /** 流水号取值函数（由引擎注入：批量时已预分配整段） */
    private java.util.function.LongFunction<Long> serialSupplier;

    public GenerateContext(String ruleCode, Map<String, String> params, java.time.LocalDateTime bizTime, Long userId) {
        this.ruleCode = ruleCode;
        this.params = params == null ? java.util.Collections.emptyMap() : params;
        this.bizTime = bizTime;
        this.userId = userId;
    }

    /**
     * 追加本段内容到前文（引擎在每段生成后调用）
     */
    public void appendPrefix(String segment) {
        this.prefix.append(segment);
    }

    /**
     * 取流水号（由引擎注入的 serialSupplier 按当前明细ID提供）
     */
    public Long nextSerial() {
        return serialSupplier.apply(currentItemId);
    }

    public void setCurrentItemId(Long currentItemId) {
        this.currentItemId = currentItemId;
    }

    public void setSerialSupplier(java.util.function.LongFunction<Long> serialSupplier) {
        this.serialSupplier = serialSupplier;
    }
}
