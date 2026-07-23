package top.mddata.base.utils;

import cn.hutool.core.util.StrUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 默认值
 *
 * @author henhen6
 * @since 2020/9/25 2:07 下午
 */
public final class DefValueHelper {
    private DefValueHelper() {
    }

    public static String getOrDef(String val, String def) {
        return StrUtil.isEmpty(val) ? def : val;
    }

    public static <T extends Serializable> T getOrDef(T val, T def) {
        return val == null ? def : val;
    }

    /**
     * Long 空值返回默认值（基本类型版本）
     *
     * @param value      可空值
     * @param defaultVal 默认值
     * @return 非空值或默认值
     */
    public static long nvl(Long value, long defaultVal) {
        return value != null ? value : defaultVal;
    }

    /**
     * 计算百分比
     *
     * @param count  分子
     * @param total  分母
     * @return 百分比值，保留2位小数
     */
    public static BigDecimal calcPercent(long count, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(count)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    /**
     * 限制值在指定范围内
     *
     * @param value 值
     * @param min   最小值
     * @param max   最大值
     * @return 限制后的值
     */
    public static int normalizeLimit(int value, int min, int max) {
        if (value <= min) {
            return min;
        }
        return Math.min(value, max);
    }

}
