package top.mddata.console.datascope;

import org.springframework.stereotype.Component;
import top.mddata.base.mybatisflex.datascope.annotation.DataScope;
import top.mddata.base.mybatisflex.datascope.model.DataScopeCurrentUser;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeCustomHandler;

/**
 * 示例（系统配置）：本人创建的参数 + 全局参数（org_id 为空）。
 *
 * <p>演示"登录上下文 + OR 复合条件"：读 currentUser.getUserId()，
 * 适合"运营维护全局参数、机构用户只看自己创建的参数+全局参数"场景。</p>
 *
 * @author henhen6
 * @since 2026-09-26
 */
@Component("configSelfAndGlobalDataScope")
public class ConfigSelfAndGlobalDataScopeHandler implements DataScopeCustomHandler {

    @Override
    public String buildCondition(DataScopeCurrentUser currentUser, DataScope dataScope, String tableAlias) {
        if (currentUser.getUserId() == null) {
            // 防御：系统线程本就会被拦截器放行，返回 null 表示本实现不产出条件
            return null;
        }
        // OR 复合条件必须整体加括号，避免与外层的 AND 发生优先级错误
        return "(created_by = %d OR org_id IS NULL)".formatted(currentUser.getUserId());
    }
}
