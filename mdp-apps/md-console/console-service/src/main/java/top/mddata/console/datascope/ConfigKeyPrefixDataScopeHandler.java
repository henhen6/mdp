package top.mddata.console.datascope;

import org.springframework.stereotype.Component;
import top.mddata.base.mybatisflex.datascope.annotation.DataScope;
import top.mddata.base.mybatisflex.datascope.model.DataScopeCurrentUser;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeCustomHandler;

/**
 * 示例（系统配置）：只能查看指定前缀的参数标识。
 *
 * <p>演示 LIKE 前缀过滤，适合"按业务域划分参数命名空间"的场景
 * （如 BASIC_ 开头的核心参数只有特定角色可见）。</p>
 *
 * @author henhen6
 * @since 2026-09-26
 */
@Component("configKeyPrefixDataScope")
public class ConfigKeyPrefixDataScopeHandler implements DataScopeCustomHandler {
    /** 允许查看的参数标识前缀 */
    private static final String KEY_PREFIX = "BASIC_";

    @Override
    public String buildCondition(DataScopeCurrentUser currentUser, DataScope dataScope, String tableAlias) {
        // String.formatted 中 %% 转义为 %，最终 SQL 为 uniq_key LIKE 'BASIC_%'
        return "uniq_key LIKE '%s%%'".formatted(KEY_PREFIX);
    }
}
