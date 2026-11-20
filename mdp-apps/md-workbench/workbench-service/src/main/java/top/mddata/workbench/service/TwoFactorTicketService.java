package top.mddata.workbench.service;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.BizException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.workbench.TwoFactorCacheKeyBuilder;
import top.mddata.common.constant.ConfigKey;
import top.mddata.console.facade.system.ConfigFacade;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 双因素认证一次性票据服务。
 *
 * <p>密码验证通过后签发，绑定用户与登录上下文；同一用户重复登录时旧票互斥作废。
 * 消费即删除（Redis DEL 返回删除条数，借此识别并发重放）；验证失败次数达到系统参数
 * “密码错误几次后锁定账号”配置的上限后，同步删除票据与用户索引，票据即时作废
 * （未配置或配置为 0 时不限制）。失败计数由 {@link CacheOps#initAndIncr(CacheKey)}
 * 原子完成（裸字符串占位携带 TTL + INCR，固定窗口不续期），不能用对象序列化通道写计数器，
 * 否则 JSON 字节与原生 INCR 编码冲突。TTL 全部由 {@link TwoFactorCacheKeyBuilder} 工厂键携带。</p>
 *
 * @author tangyh
 */
@Service
@RequiredArgsConstructor
public class TwoFactorTicketService {

    /**
     * 计数键/票据键/索引键读取统一禁止空值回写。这些都是易失流程键而非数据库数据，
     * 框架默认的 NullVal 回写会在 GET miss 时凭空占位：污染计数键后 INCR 报
     * "value is not an integer"，污染票据键则破坏 peek 的“不存在即 null”语义。
     */
    private static final boolean NO_NULL_CACHE = false;

    /**
     * 失败计数键的场景段，与 OTP 计数共用 5 分钟有效期；
     * 最终键格式为 two_factor:ticket.fail.otp:{ticket}
     */
    private static final String FAIL_SCENE = "ticket.fail";

    /** 票据随机源：票据是 bearer 凭证，必须使用密码学安全随机数，不能用 ThreadLocalRandom */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** 缓存操作通道：票据、用户索引、失败计数均存 Redis，TTL 由缓存键携带 */
    private final CacheOps cacheOps;

    /** 系统参数读取：票据失败次数上限复用密码错误锁定账号配置 */
    private final ConfigFacade configFacade;

    /**
     * 票据载荷：登录上下文 + 二次验证目标
     */
    @Data
    public static class TwoFactorTicketPayload {
        /** 登录用户 id，verify 通过后据此重新加载用户并复核状态 */
        private Long userId;
        /** 登录认证类型（CAPTCHA/USERNAME），随会话签发写入登录日志 */
        private String authType;
        /** 登录账号（用户名/手机号/邮箱，用于登录日志） */
        private String account;
        /** 登录设备 id，随会话签发透传（未传时由登录编排随机生成） */
        private String deviceId;
        /** 登录设备信息，随会话签发透传 */
        private String deviceInfo;
        /**
         * 用户开通的验证渠道 PHONE/EMAIL/BOTH：票据签发时确定后固定不变，
         * 作为“换一种方式”是否允许的判定依据
         */
        private String openedChannel;
        /**
         * 当前实际投递渠道 PHONE/EMAIL：BOTH 用户首次默认手机，随“换一种方式”回写。
         * 只表示本次验证码发到哪里，绝不能当作开通渠道参与切换校验
         */
        private String channel;
        /** OTP 实际接收目标（明文，仅存在于 Redis，不回传前端） */
        private String target;
        /** 用户手机号快照：默认重发与同渠道重发沿用，显式换渠道时实时重查绑定 */
        private String phone;
        /** 用户邮箱快照：默认重发与同渠道重发沿用，显式换渠道时实时重查绑定 */
        private String email;
    }

    /**
     * 签发票据，同一用户的旧票据即时作废（含旧票失败计数）。
     *
     * @param payload 票据载荷
     * @return 32 字节随机串生成的 URL-safe 票据
     */
    public String issue(TwoFactorTicketPayload payload) {
        ArgumentAssert.notNull(payload, () -> new BizException("票据载荷不能为空"));
        ArgumentAssert.notNull(payload.getUserId(), () -> new BizException("票据载荷缺少用户标识"));

        // 通过用户维度索引找到旧票并删除，实现同用户互斥
        CacheKey indexKey = TwoFactorCacheKeyBuilder.ticketIndex(payload.getUserId());
        String oldTicket = cacheOps.<String>get(indexKey, NO_NULL_CACHE).getValue();
        if (StrUtil.isNotBlank(oldTicket)) {
            // 旧票与其失败计数一并清理，避免计数残留干扰新票
            // 注意：get+del+set 非原子，同用户并发签发存在竞争窗口；前置已有密码校验，
            // 窗口极小且后果仅为旧票延迟作废，业务上可接受
            cacheOps.del(TwoFactorCacheKeyBuilder.ticket(oldTicket),
                    TwoFactorCacheKeyBuilder.otp(FAIL_SCENE, oldTicket));
        }

        String ticket = newTicket();
        cacheOps.set(TwoFactorCacheKeyBuilder.ticket(ticket), payload);
        cacheOps.set(indexKey, ticket);
        return ticket;
    }

    /**
     * 回写票据载荷（send 换渠道后仅更新投递渠道 channel/target，openedChannel 保持不变）。
     *
     * <p>仅当票据仍然存在（未过期、未作废、未超限）时才回写，避免对已失效票据凭空重建
     * 缓存键。工厂键自带 5 分钟 TTL，回写会把 TTL 重置为完整 5 分钟。</p>
     *
     * @param ticket  票据
     * @param payload 新载荷
     */
    public void updatePayload(String ticket, TwoFactorTicketPayload payload) {
        ArgumentAssert.isTrue(StrUtil.isNotBlank(ticket),
                () -> new BizException("票据不能为空"));
        ArgumentAssert.notNull(payload, () -> new BizException("票据载荷不能为空"));
        if (peek(ticket) == null) {
            throw new BizException("票据已失效，请重新登录");
        }
        cacheOps.set(TwoFactorCacheKeyBuilder.ticket(ticket), payload);
    }

    /**
     * 只读校验（发送验证码前使用），不消费票据。票据不存在或失败超限时返回 null。
     *
     * @param ticket 票据
     * @return 票据载荷；不存在或已作废时返回 null
     */
    public TwoFactorTicketPayload peek(String ticket) {
        ArgumentAssert.isTrue(StrUtil.isNotBlank(ticket), () -> new BizException("票据不能为空"));
        if (isBlocked(ticket)) {
            return null;
        }
        CacheResult<TwoFactorTicketPayload> result =
                cacheOps.get(TwoFactorCacheKeyBuilder.ticket(ticket), NO_NULL_CACHE);
        return result.getValue();
    }

    /**
     * 一次性消费：DEL 返回 0 说明票据已被并发请求消费，本次返回 null。
     * 已因失败超限作废的票据在此防御性拦截，直接返回 null。
     *
     * @param ticket 票据
     * @return 票据载荷；不存在或已被消费时返回 null
     */
    public TwoFactorTicketPayload consume(String ticket) {
        ArgumentAssert.isTrue(StrUtil.isNotBlank(ticket), () -> new BizException("票据不能为空"));
        if (isBlocked(ticket)) {
            return null;
        }
        CacheKey ticketKey = TwoFactorCacheKeyBuilder.ticket(ticket);
        TwoFactorTicketPayload payload = cacheOps.<TwoFactorTicketPayload>get(ticketKey, NO_NULL_CACHE).getValue();
        if (payload == null) {
            return null;
        }
        Long removed = cacheOps.del(ticketKey);
        if (removed == null || removed == 0L) {
            return null;
        }
        cacheOps.del(TwoFactorCacheKeyBuilder.ticketIndex(payload.getUserId()));
        return payload;
    }

    /**
     * 记录一次验证失败。计数由 initAndIncr 原子完成：首次以裸字符串 "0" 占位携带固定窗口
     * TTL（不续期）再 INCR，已存在则直接 INCR；失败次数达到系统配置上限时同步删除票据键与
     * 用户索引键并抛出异常，计数键保留至 TTL 到期以持续拦截；未配置或配置为 0 时只累计不拦截。
     *
     * @param ticket 票据
     */
    public void recordFail(String ticket) {
        ArgumentAssert.isTrue(StrUtil.isNotBlank(ticket), () -> new BizException("票据不能为空"));
        CacheKey failKey = TwoFactorCacheKeyBuilder.otp(FAIL_SCENE, ticket);
        long count = cacheOps.initAndIncr(failKey);
        Integer max = maxFailCount();
        // 计数始终累计：未配置（0）或未达上限直接放行，管理员调小或启用上限后存量计数立即生效
        if (max == null || max <= 0 || count < max) {
            return;
        }

        // 失败超限：票据即时作废。需先读载荷拿 userId 才能清理用户索引键
        CacheKey ticketKey = TwoFactorCacheKeyBuilder.ticket(ticket);
        TwoFactorTicketPayload payload = cacheOps.<TwoFactorTicketPayload>get(ticketKey, NO_NULL_CACHE).getValue();
        if (payload != null) {
            cacheOps.del(ticketKey, TwoFactorCacheKeyBuilder.ticketIndex(payload.getUserId()));
        }
        throw new BizException("验证失败次数过多，请重新登录");
    }

    /**
     * 判断票据是否因失败次数达到系统配置上限而作废；未配置或配置为 0 时恒为 false。
     *
     * @param ticket 票据
     * @return true 表示已作废
     */
    public boolean isBlocked(String ticket) {
        ArgumentAssert.isTrue(StrUtil.isNotBlank(ticket), () -> new BizException("票据不能为空"));
        // 计数器由 stringRedisTemplate 以裸字符串写入，对象通道读回可能是 Integer/String，
        // 用 asLong 归一，不能强转 Long；并显式禁止 NullVal 回写，避免 miss 时占位计数键
        Long count = cacheOps.get(TwoFactorCacheKeyBuilder.otp(FAIL_SCENE, ticket), NO_NULL_CACHE).asLong();
        Integer max = maxFailCount();
        return max != null && max > 0 && count != null && count >= max;
    }

    /**
     * 票据校验最大失败次数，复用“密码错误几次后锁定账号”系统配置；
     * 未配置或配置为 0 表示不限制（与密码错误锁定语义一致）
     */
    private Integer maxFailCount() {
        return configFacade.getInteger(ConfigKey.Workbench.PASSWORD_ERROR_LOCK_ACCOUNT, 0);
    }

    /**
     * 生成 32 字节 URL-safe 随机票据（bearer 凭证，必须使用 SecureRandom）
     */
    private String newTicket() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
