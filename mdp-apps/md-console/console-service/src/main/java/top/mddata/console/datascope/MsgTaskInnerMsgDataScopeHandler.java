package top.mddata.console.datascope;

import org.springframework.stereotype.Component;
import top.mddata.base.mybatisflex.datascope.annotation.DataScope;
import top.mddata.base.mybatisflex.datascope.model.DataScopeCurrentUser;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeCustomHandler;

/**
 * 示例（消息任务）：只能查看站内信任务。
 *
 * <p>演示"枚举业务维度"过滤：mdc_msg_task 无组织列，
 * 内置组织档不可用，自定义实现按 type 字段过滤正合适。</p>
 *
 * @author henhen6
 * @since 2026-09-26
 */
@Component("msgTaskInnerMsgDataScope")
public class MsgTaskInnerMsgDataScopeHandler implements DataScopeCustomHandler {
    /** 消息类型：1-站内信 */
    private static final int TYPE_INNER_MSG = 1;

    @Override
    public String buildCondition(DataScopeCurrentUser currentUser, DataScope dataScope, String tableAlias) {
        return "type = " + TYPE_INNER_MSG;
    }
}
