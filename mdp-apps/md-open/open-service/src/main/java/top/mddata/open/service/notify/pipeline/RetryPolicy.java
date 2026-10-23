package top.mddata.open.service.notify.pipeline;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 重试策略：构造时解析重试梯度配置，非法配置立即 fail fast（避免运行期配置错误被静默当成"重试用尽"）。
 * 配置格式示例："2m,5m,30m,6h,12h"，单位仅支持 m(分)/h(时)/d(天)。
 */
public class RetryPolicy {

    private final List<Duration> intervals;

    public RetryPolicy(String timeLevel) {
        this.intervals = parse(timeLevel);
    }

    static List<Duration> parse(String timeLevel) {
        if (timeLevel == null || timeLevel.isBlank()) {
            throw new IllegalArgumentException("重试间隔配置不能为空，格式示例：2m,5m,30m,6h,12h");
        }
        String[] parts = timeLevel.split(",");
        List<Duration> result = new ArrayList<>(parts.length);
        for (String part : parts) {
            result.add(parseOne(part.trim().toLowerCase(), part));
        }
        return List.copyOf(result);
    }

    private static Duration parseOne(String exp, String origin) {
        if (exp.length() < 2) {
            throw new IllegalArgumentException("重试间隔配置项非法：" + origin);
        }
        long value;
        try {
            value = Long.parseLong(exp.substring(0, exp.length() - 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("重试间隔配置项数值非法：" + origin, e);
        }
        if (value <= 0) {
            throw new IllegalArgumentException("重试间隔必须为正数：" + origin);
        }
        return switch (exp.charAt(exp.length() - 1)) {
            case 'm' -> Duration.ofMinutes(value);
            case 'h' -> Duration.ofHours(value);
            case 'd' -> Duration.ofDays(value);
            default -> throw new IllegalArgumentException("重试间隔单位仅支持 m/h/d：" + origin);
        };
    }

    /**
     * 计算下次重试时间。
     *
     * @param requestCnt 已投递次数（含本次）
     * @param now        当前时间
     * @return null 表示重试次数已用尽
     */
    public LocalDateTime nextRequestTime(int requestCnt, LocalDateTime now) {
        if (requestCnt >= intervals.size()) {
            return null;
        }
        return now.withSecond(0).withNano(0).plus(intervals.get(requestCnt - 1));
    }

    /** 最大投递次数 = 梯度级数 */
    public int getMaxRetry() {
        return intervals.size();
    }
}
