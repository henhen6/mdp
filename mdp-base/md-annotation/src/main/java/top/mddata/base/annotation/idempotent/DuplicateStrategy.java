package top.mddata.base.annotation.idempotent;

/** 首次请求成功后，重复请求的处理策略。 */
public enum DuplicateStrategy {
    /** 返回首次成功响应。 */
    REPLAY,
    /** 返回重复请求错误。 */
    REJECT
}
