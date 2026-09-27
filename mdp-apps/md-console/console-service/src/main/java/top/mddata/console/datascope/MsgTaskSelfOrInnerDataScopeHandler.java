package top.mddata.console.datascope;

import org.springframework.stereotype.Component;
import top.mddata.base.mybatisflex.datascope.annotation.DataScope;
import top.mddata.base.mybatisflex.datascope.model.DataScopeCurrentUser;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeCustomHandler;

/**
 * 示例（消息任务）：本人创建的任务 + 全部站内信任务。
 *
 * <p>演示"登录上下文 + OR 复合条件"：本人发起的各渠道任务可见，
 * 站内信全员可见，短信/邮件任务只能看自己创建的。</p>
 *
 * @author henhen6
 * @since 2026-09-26
 */
@Component("msgTaskSelfOrInnerDataScope")
public class MsgTaskSelfOrInnerDataScopeHandler implements DataScopeCustomHandler {
    /** 消息类型：1-站内信 */
    private static final int TYPE_INNER_MSG = 1;

    @Override
    public String buildCondition(DataScopeCurrentUser currentUser, DataScope dataScope, String tableAlias) {
        if (currentUser.getUserId() == null) {
            return null;
        }
        // OR 复合条件必须整体加括号，避免与外层的 AND 发生优先级错误
        return "(created_by = %d OR type = %d)".formatted(currentUser.getUserId(), TYPE_INNER_MSG);
    }
}
