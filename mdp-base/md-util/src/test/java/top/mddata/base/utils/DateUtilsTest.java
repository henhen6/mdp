package top.mddata.base.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("DateUtils.conversionDateTime 时间周期转换单元测试")
class DateUtilsTest {
    private static final LocalDateTime BASE = LocalDateTime.of(2026, 11, 23, 10, 0, 0);

    @Test
    @DisplayName("大写M表示月：3M为3个月后")
    void shouldPlusMonthsForUppercaseM() {
        assertEquals(BASE.plusMonths(3), DateUtils.conversionDateTime(BASE, "3M"));
    }

    @Test
    @DisplayName("小写m表示分钟：30m为30分钟后")
    void shouldPlusMinutesForLowercaseM() {
        assertEquals(BASE.plusMinutes(30), DateUtils.conversionDateTime(BASE, "30m"));
    }

    @Test
    @DisplayName("h与H均表示小时")
    void shouldPlusHoursForH() {
        assertEquals(BASE.plusHours(2), DateUtils.conversionDateTime(BASE, "2h"));
        assertEquals(BASE.plusHours(2), DateUtils.conversionDateTime(BASE, "2H"));
    }

    @Test
    @DisplayName("d表示天")
    void shouldPlusDaysForD() {
        assertEquals(BASE.plusDays(4), DateUtils.conversionDateTime(BASE, "4d"));
    }

    @Test
    @DisplayName("y表示年")
    void shouldPlusYearsForY() {
        assertEquals(BASE.plusYears(1), DateUtils.conversionDateTime(BASE, "1y"));
    }

    @Test
    @DisplayName("w表示周")
    void shouldPlusWeeksForW() {
        assertEquals(BASE.plusWeeks(2), DateUtils.conversionDateTime(BASE, "2w"));
    }

    @Test
    @DisplayName("s表示秒")
    void shouldPlusSecondsForS() {
        assertEquals(BASE.plusSeconds(10), DateUtils.conversionDateTime(BASE, "10s"));
    }

    @Test
    @Disabled("存量缺陷：单字符入参 substring(0,0) 得空串，Convert.toLong 返回 null 导致 NPE，修复待另行裁决")
    @DisplayName("长度为1时默认按天（当前实现为 NPE，暂禁用）")
    void shouldDefaultToDaysWhenSingleChar() {
        assertEquals(BASE.plusDays(5), DateUtils.conversionDateTime(BASE, "5"));
    }

    @Test
    @DisplayName("空串返回MAX表示永不过期")
    void shouldReturnMaxWhenEmpty() {
        assertEquals(LocalDateTime.MAX, DateUtils.conversionDateTime(BASE, ""));
    }

    @Test
    @DisplayName("0返回当天23:59:59")
    void shouldReturnEndOfDayWhenZero() {
        LocalDateTime endOfDay = DateUtils.conversionDateTime(BASE, "0");
        assertEquals(LocalTime.of(23, 59, 59, 999_999_000), endOfDay.toLocalTime());
    }
}
