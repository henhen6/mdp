package top.mddata.console.datascope;

import org.springframework.stereotype.Component;
import top.mddata.base.mybatisflex.datascope.annotation.DataScope;
import top.mddata.base.mybatisflex.datascope.model.DataScopeCurrentUser;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeCustomHandler;

/**
 * 示例（系统配置）：只能查看指定配置组的参数。
 *
 * <p>演示"固定业务维度"过滤：条件不依赖登录上下文，
 * 适用于"某角色只准维护 message 组参数"这类场景。</p>
 *
 * @author henhen6
 * @since 2026-09-26
 */
@Component("configGroupDataScope")
public class ConfigGroupDataScopeHandler implements DataScopeCustomHandler {
    /** 允许查看的配置组（示例硬编码，生产应来自配置或权限数据） */
    private static final String ALLOWED_GROUP = "message";

    @Override
    public String buildCondition(DataScopeCurrentUser currentUser, DataScope dataScope, String tableAlias) {
        return "config_group = '%s'".formatted(ALLOWED_GROUP);
    }
}
