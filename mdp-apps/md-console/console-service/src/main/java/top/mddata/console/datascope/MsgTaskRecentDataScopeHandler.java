package top.mddata.console.datascope;

import org.springframework.stereotype.Component;
import top.mddata.base.mybatisflex.datascope.annotation.DataScope;
import top.mddata.base.mybatisflex.datascope.model.DataScopeCurrentUser;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeCustomHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 示例（消息任务）：只能查看近 30 天创建的任务。
 *
 * <p>演示两个技巧：</p>
 * <ul>
 *   <li>时间在 Java 侧计算后内联，避免 DATE_SUB 等方言函数（兼容达梦）</li>
 *   <li>tableAlias 判空拼列名：单表无别名用裸列，多表查询用 alias.列名</li>
 * </ul>
 *
 * @author henhen6
 * @since 2026-09-26
 */
@Component("msgTaskRecentDataScope")
public class MsgTaskRecentDataScopeHandler implements DataScopeCustomHandler {
    /** 时间窗口天数 */
    private static final int RECENT_DAYS = 30;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String buildCondition(DataScopeCurrentUser currentUser, DataScope dataScope, String tableAlias) {
        String since = LocalDateTime.now().minusDays(RECENT_DAYS).format(FORMATTER);
        String column = tableAlias == null ? "created_at" : tableAlias + ".created_at";
        return "%s >= '%s'".formatted(column, since);
    }
}
