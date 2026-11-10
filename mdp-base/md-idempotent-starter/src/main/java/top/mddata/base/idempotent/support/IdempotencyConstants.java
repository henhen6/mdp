package top.mddata.base.idempotent.support;

import top.mddata.base.constant.ContextConstants;

/**
 * 幂等协议常量。
 *
 * @author henhen6
 */
public final class IdempotencyConstants {

    /** 幂等令牌请求头名称。 */
    public static final String HEADER = "X-Idempotency-Key";

    /** 请求属性键：抢占成功后保存令牌与执行身份的绑定关系，供后续拦截器读取。 */
    public static final String EXECUTION_ATTRIBUTE = ContextConstants.IDEMPOTENCY_EXECUTION_ATTRIBUTE;

    private IdempotencyConstants() {
    }
}
