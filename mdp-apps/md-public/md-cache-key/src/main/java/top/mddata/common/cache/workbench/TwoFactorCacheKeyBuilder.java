package top.mddata.common.cache.workbench;

import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.common.cache.CacheKeyTable;

import java.time.Duration;

/**
 * 双因素认证缓存 KEY：ticket 5 分钟、OTP 5 分钟、发送间隔 60 秒。
 * 场景隔离：登录二次验证与开通验证使用不同 scene，杜绝跨场景用码。
 * <p>
 * scene 取值约束：仅允许小写字母、数字、英文点号(.)和英文半角冒号(:)，
 * 禁止下划线、空格及其他特殊字符；内置场景常量见 {@link #SCENE_LOGIN}、{@link #SCENE_MANAGE}。
 *
 * @author tangyh
 */
public class TwoFactorCacheKeyBuilder implements CacheKeyBuilder {

    /** 登录二次验证 OTP 场景 */
    public static final String SCENE_LOGIN = "login";
    /** 开通/关闭/换渠道验证 OTP 场景 */
    public static final String SCENE_MANAGE = "manage";
    /** 忘记密码验证 OTP 场景 */
    public static final String SCENE_FORGET_PASSWORD = "forget.password";

    /** ticket 有效期：5 分钟 */
    private static final Duration TICKET_EXPIRE = Duration.ofMinutes(5);
    /** OTP 验证码有效期：5 分钟 */
    private static final Duration OTP_EXPIRE = Duration.ofMinutes(5);
    /** OTP 发送间隔限制有效期：60 秒 */
    private static final Duration OTP_LIMIT_EXPIRE = Duration.ofSeconds(60);

    /** key 的业务字段 */
    private final String field;
    /** 工厂烘入的过期时间 */
    private final Duration expire;

    /**
     * 公共无参构造，与同目录其他缓存键构建器风格保持一致；
     * 业务代码请使用静态工厂，以获得正确的业务字段与过期时间。
     */
    public TwoFactorCacheKeyBuilder() {
        this(null, null);
    }

    private TwoFactorCacheKeyBuilder(String field, Duration expire) {
        this.field = field;
        this.expire = expire;
    }

    /**
     * 登录二次验证一次性票据键，TTL 5 分钟。
     * 同一用户重复签发时经 {@link #ticketIndex(Long)} 索引互斥作废旧票。
     *
     * @param ticket 一次性票据值
     * @return 自带 TTL 的缓存键
     */
    public static CacheKey ticket(String ticket) {
        return new TwoFactorCacheKeyBuilder("ticket", TICKET_EXPIRE).key(ticket);
    }

    /** 用户维度票据索引，用于同一用户重复登录时作废旧 ticket */
    public static CacheKey ticketIndex(Long userId) {
        return new TwoFactorCacheKeyBuilder("ticket.user", TICKET_EXPIRE).key(userId);
    }

    /**
     * OTP 验证码键，TTL 5 分钟；按 scene + target 隔离，杜绝跨场景、跨目标用码。
     * 失败计数键复用本工厂（scene 传 "{scene}.fail" 或 "ticket.fail"），
     * 计数器由 stringRedisTemplate 裸字符串写入，依赖键携带的固定窗口 TTL。
     *
     * @param scene  场景标识，取 {@link #SCENE_LOGIN}、{@link #SCENE_MANAGE} 等常量
     * @param target 接收目标（手机号或邮箱）
     * @return 自带 TTL 的缓存键
     */
    public static CacheKey otp(String scene, String target) {
        return new TwoFactorCacheKeyBuilder(scene + ".otp", OTP_EXPIRE).key(target);
    }

    /** OTP 发送间隔限制 key */
    public static CacheKey otpLimit(String scene, String target) {
        return new TwoFactorCacheKeyBuilder(scene + ".otp.limit", OTP_LIMIT_EXPIRE).key(target);
    }

    /** 业务类型固定为双因素认证（CacheKeyTable.TWO_FACTOR） */
    @Override
    public String getTable() {
        return CacheKeyTable.TWO_FACTOR;
    }

    /** 工厂注入的业务字段段（ticket / ticket.user / {scene}.otp 等） */
    @Override
    public String getField() {
        return field;
    }

    /** 工厂注入的 TTL：键的过期时间在静态工厂中按语义确定，不在调用方传入 */
    @Override
    public Duration getExpire() {
        return expire;
    }
}
