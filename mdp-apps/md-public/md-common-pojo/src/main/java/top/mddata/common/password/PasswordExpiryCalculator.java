package top.mddata.common.password;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import top.mddata.base.utils.DateUtils;

/** 密码过期状态纯计算，不读取配置和数据库。 */
public final class PasswordExpiryCalculator {

    private PasswordExpiryCalculator() {
    }

    public enum Status {
        NEVER, NORMAL, EXPIRING, EXPIRED
    }

    public record Result(Status status, Long remainDays) {
    }

    public static Result evaluate(LocalDateTime now, LocalDateTime expireTime, int remindDays) {
        if (remindDays < 0) {
            throw new IllegalArgumentException("密码临期提醒天数不能小于0");
        }
        if (expireTime == null || LocalDateTime.MAX.equals(expireTime)) {
            return new Result(Status.NEVER, null);
        }
        if (!now.isBefore(expireTime)) {
            return new Result(Status.EXPIRED, 0L);
        }
        long days = ChronoUnit.DAYS.between(now.toLocalDate(), expireTime.toLocalDate());
        Status status = remindDays > 0 && days <= remindDays ? Status.EXPIRING : Status.NORMAL;
        return new Result(status, days);
    }

    /**
     * 密码变更场景的过期时间续期策略（规格 §5.2.1）：现有值为 null 表示该用户豁免
     * 密码过期，返回 null 且调用点必须跳过 setPwExpireTime（UpdateEntity 未 set 的
     * 字段不进 UPDATE 语句）；有值（含 MAX）才按当前周期重算。
     */
    public static LocalDateTime renewExpireTime(LocalDateTime oldExpireTime, String period) {
        if (oldExpireTime == null) {
            return null;
        }
        return DateUtils.conversionDateTime(LocalDateTime.now(), period);
    }
}
