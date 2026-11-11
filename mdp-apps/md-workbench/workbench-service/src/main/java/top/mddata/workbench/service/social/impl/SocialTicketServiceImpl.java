package top.mddata.workbench.service.social.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.BizException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.workbench.SocialPendingCacheKeyBuilder;
import top.mddata.common.cache.workbench.SocialReauthCacheKeyBuilder;
import top.mddata.common.cache.workbench.SocialStateCacheKeyBuilder;
import top.mddata.workbench.enumeration.SocialActionEnum;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.social.SocialTicketService;
import top.mddata.workbench.service.social.model.PendingSocialUser;
import top.mddata.workbench.service.social.model.SocialIdentity;
import top.mddata.workbench.service.social.model.SocialReauth;
import top.mddata.workbench.service.social.model.SocialState;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 基于 {@link CacheOps} 的一次性票据服务实现。
 *
 * @author tangyh
 */
@Service
@RequiredArgsConstructor
public class SocialTicketServiceImpl implements SocialTicketService {

    /** 待选择票据凭据校验最大失败次数 */
    private static final int MAX_FAILURES = 5;
    /** 消费标记初始值 */
    private static final Object CONSUMED = Boolean.TRUE;
    /** 失败计数初始值，先占位以携带 TTL */
    private static final Long INITIAL_FAILURES = 0L;
    /** 消费标记键段 */
    private static final String CONSUMED_SEGMENT = "consumed";
    /** 失败计数键段 */
    private static final String FAILED_SEGMENT = "failed";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final CacheOps cacheOps;
    private final SocialStateCacheKeyBuilder stateBuilder = new SocialStateCacheKeyBuilder();
    private final SocialPendingCacheKeyBuilder pendingBuilder = new SocialPendingCacheKeyBuilder();
    private final SocialReauthCacheKeyBuilder reauthBuilder = new SocialReauthCacheKeyBuilder();

    @Override
    public String createState(SocialState state) {
        ArgumentAssert.notNull(state, "授权状态不能为空");
        String ticket = newTicket();
        cacheOps.set(stateBuilder.key(ticket), state);
        return ticket;
    }

    @Override
    public SocialState consumeState(String ticket, SocialProviderEnum provider,
                                    SocialActionEnum action) {
        CacheResult<SocialState> result = cacheOps.get(stateBuilder.key(ticket));
        SocialState state = result.getValue();
        ArgumentAssert.notNull(state,
                () -> new BizException("授权状态已失效，请重新发起"));
        ArgumentAssert.isTrue(state.getProvider() == provider && state.getAction() == action,
                () -> new BizException("授权状态与当前操作不匹配"));
        ArgumentAssert.isTrue(cacheOps.setIfAbsent(stateBuilder.key(CONSUMED_SEGMENT, ticket),
                CONSUMED), () -> new BizException("授权状态已使用，请重新发起"));
        return state;
    }

    @Override
    public String createPending(SocialIdentity identity) {
        ArgumentAssert.notNull(identity, "第三方身份不能为空");
        String ticket = newTicket();
        cacheOps.set(pendingBuilder.key(ticket), new PendingSocialUser(identity));
        return ticket;
    }

    @Override
    public PendingSocialUser getPending(String ticket) {
        PendingSocialUser pending = getCachePending(ticket);
        ArgumentAssert.isFalse(cacheOps.exists(pendingBuilder.key(CONSUMED_SEGMENT, ticket)),
                () -> new BizException("待选择票据已使用，请重新授权"));
        return pending;
    }

    @Override
    public void consumePending(String ticket) {
        getCachePending(ticket);
        ArgumentAssert.isTrue(cacheOps.setIfAbsent(pendingBuilder.key(CONSUMED_SEGMENT, ticket),
                CONSUMED), () -> new BizException("待选择票据已使用，请重新授权"));
    }

    @Override
    public void registerPendingFailure(String ticket) {
        CacheKey failedKey = pendingBuilder.key(FAILED_SEGMENT, ticket);
        // incr 新建的计数器没有过期时间，先占位携带与票据一致的 TTL
        cacheOps.setIfAbsent(failedKey, INITIAL_FAILURES);
        long failures = cacheOps.incr(failedKey);
        if (failures >= MAX_FAILURES) {
            cacheOps.del(pendingBuilder.key(ticket), failedKey);
        }
    }

    @Override
    public String createReauth(SocialReauth reauth) {
        ArgumentAssert.notNull(reauth, "身份验证票据不能为空");
        String ticket = newTicket();
        cacheOps.set(reauthBuilder.key(ticket), reauth);
        return ticket;
    }

    @Override
    public SocialReauth getReauth(String ticket, Long userId, SocialActionEnum action,
                                  SocialProviderEnum provider) {
        SocialReauth reauth = getCacheTicket(ticket);
        ArgumentAssert.isFalse(cacheOps.exists(reauthBuilder.key(CONSUMED_SEGMENT, ticket)),
                () -> new BizException("身份验证票据已使用"));
        ArgumentAssert.isTrue(reauth.getUserId().equals(userId)
                        && reauth.getAction() == action && reauth.getProvider() == provider,
                () -> new BizException("身份验证票据与当前操作不匹配"));
        return reauth;
    }

    @Override
    public void consumeReauth(String ticket) {
        getCacheTicket(ticket);
        ArgumentAssert.isTrue(cacheOps.setIfAbsent(reauthBuilder.key(CONSUMED_SEGMENT, ticket),
                CONSUMED), () -> new BizException("身份验证票据已使用"));
    }

    /**
     * 只负责读取待选择票据并断言存在
     */
    private PendingSocialUser getCachePending(String ticket) {
        CacheResult<PendingSocialUser> result = cacheOps.get(pendingBuilder.key(ticket));
        PendingSocialUser pending = result.getValue();
        return ArgumentAssert.notNull(pending,
                () -> new BizException("待选择票据已失效，请重新授权"));
    }

    /**
     * 只负责读取二次验证票据并断言存在
     */
    private SocialReauth getCacheTicket(String ticket) {
        CacheResult<SocialReauth> result = cacheOps.get(reauthBuilder.key(ticket));
        SocialReauth reauth = result.getValue();
        return ArgumentAssert.notNull(reauth,
                () -> new BizException("身份验证已过期，请重新验证"));
    }

    /**
     * 生成 32 字节 URL-safe 随机票据
     */
    private String newTicket() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
