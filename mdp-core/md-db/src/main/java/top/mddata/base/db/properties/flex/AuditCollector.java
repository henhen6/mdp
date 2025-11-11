package top.mddata.base.db.properties.flex;

/**
 * mybatis-flex 审计实现类型
 * @author henhen6
 * @since 2024年06月06日00:13:28
 */
public enum AuditCollector {
    /** slf4j 输出到日志文件 */
    DEFAULTS,
    /** 使用其把消息输出到控制台 */
    CONSOLE,
    /** 计数消息收集器，当消息达到指定数量时发送消息 */
    COUNTABLE,
    /** 定时把消息通过 MessageReporter 发送到指定位置 */
    SCHEDULED;
}
