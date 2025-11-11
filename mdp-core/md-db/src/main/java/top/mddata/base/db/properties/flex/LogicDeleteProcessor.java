package top.mddata.base.db.properties.flex;

/**
 * mybatis-flex 逻辑删除处理器类型
 *
 * 参考： https://mybatis-flex.com/zh/core/logic-delete.html#%E5%86%85%E7%BD%AE%E9%80%BB%E8%BE%91%E5%88%A0%E9%99%A4%E5%A4%84%E7%90%86%E5%99%A8
 *
 * @author henhen6
 * @since 2024年06月06日00:13:28
 */
public enum LogicDeleteProcessor {
    /** integer 数据正常时的值 0	数据被删除时的值 1 */
    INTEGER_LOGIC_DELETE_PROCESSOR,
    /** tinyint 数据正常时的值 false	数据被删除时的值 true */
    BOOLEAN_LOGIC_DELETE_PROCESSOR,
    /** datetime 数据正常时的值 null	数据被删除时的值 	被删除时间 */
    DATE_TIME_LOGIC_DELETE_PROCESSOR,
    /** bigint	 数据正常时的值 0	数据被删除时的值 被删除时的时间戳 */
    TIME_STAMP_LOGIC_DELETE_PROCESSOR,
    /** 该条数据的主键类型	 数据正常时的值 null	数据被删除时的值 该条数据的主键值 */
    PRIMARY_KEY_LOGIC_DELETE_PROCESSOR,
    /** bigint  数据正常时的值 0	数据被删除时的值 被删除时的时间戳  同时更新delBy字段 */
    TIME_STAMP_DEL_BY_LOGIC_DELETE_PROCESSOR;
}
