package top.mddata.workbench.service.twofactor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.mddata.console.facade.system.ConfigFacade;
import top.mddata.workbench.enumeration.TwoFactorChannelEnum;
import top.mddata.workbench.enumeration.TwoFactorStrategyEnum;

import java.time.Instant;

/**
 * 双因素认证全局策略解析器。
 *
 * <p>每次登录都要判定是否触发二次验证，为避免登录链路查库，配置做 60 秒本地缓存；
 * 策略读取异常时回退 OFF（可用性优先，放行不阻断登录），强制渠道读取异常或配置值非法时
 * 回退 BOTH。</p>
 *
 * @author tangyh
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TwoFactorStrategyResolver {

    /** 全局策略的系统配置键（mdc_config.uniq_key，SECURITY 组） */
    public static final String CONFIG_KEY = "TWO_FACTOR_STRATEGY";

    /** FORCE 档位验证渠道的系统配置键（mdc_config.uniq_key，SECURITY 组） */
    public static final String FORCE_CHANNEL_CONFIG_KEY = "TWO_FACTOR_FORCE_CHANNEL";

    /** 配置本地缓存时长：60 秒内重复读取不再查库，避免登录热路径反复访问 */
    private static final long CACHE_MILLIS = 60_000L;

    /** 系统配置读取通道（console 跨模块 Facade） */
    private final ConfigFacade configFacade;

    /** 全局策略的缓存时间戳，配合 {@link #CACHE_MILLIS} 判定有效期 */
    private volatile Instant cachedAt = Instant.EPOCH;

    /** 全局策略缓存值，初始 OFF（配置读取失败时的兜底） */
    private volatile TwoFactorStrategyEnum cached = TwoFactorStrategyEnum.OFF;

    /** 强制渠道的缓存时间戳，与全局策略缓存相互独立 */
    private volatile Instant forceChannelCachedAt = Instant.EPOCH;

    /** 强制渠道缓存值，初始 BOTH（读取异常或配置非法时的兜底） */
    private volatile TwoFactorChannelEnum forceChannelCached = TwoFactorChannelEnum.BOTH;

    /**
     * 解析当前全局策略
     *
     * @return 全局策略，读取异常时返回 OFF
     */
    public TwoFactorStrategyEnum resolve() {
        if (isCacheValid()) {
            return cached;
        }
        synchronized (this) {
            if (isCacheValid()) {
                return cached;
            }
            cached = load();
            cachedAt = Instant.now();
            return cached;
        }
    }

    /**
     * 解析 FORCE 档位指定的验证渠道
     *
     * @return 强制渠道配置，读取异常或配置值非法时回退 BOTH
     */
    public TwoFactorChannelEnum resolveForceChannel() {
        if (isForceChannelCacheValid()) {
            return forceChannelCached;
        }
        synchronized (this) {
            if (isForceChannelCacheValid()) {
                return forceChannelCached;
            }
            forceChannelCached = loadForceChannel();
            forceChannelCachedAt = Instant.now();
            return forceChannelCached;
        }
    }

    /** 全局策略缓存是否仍在 {@link #CACHE_MILLIS} 有效期内 */
    private boolean isCacheValid() {
        return cachedAt.plusMillis(CACHE_MILLIS).isAfter(Instant.now());
    }

    /** 强制渠道缓存是否仍在 {@link #CACHE_MILLIS} 有效期内 */
    private boolean isForceChannelCacheValid() {
        return forceChannelCachedAt.plusMillis(CACHE_MILLIS).isAfter(Instant.now());
    }

    /**
     * 从系统配置加载全局策略；读取异常时 error 日志并回退 OFF，
     * 保证配置故障不阻断登录链路。
     */
    private TwoFactorStrategyEnum load() {
        try {
            return TwoFactorStrategyEnum.of(configFacade.getString(CONFIG_KEY, "OFF"));
        } catch (Exception e) {
            log.error("[双因素认证] 读取全局策略失败，回退 OFF", e);
            return TwoFactorStrategyEnum.OFF;
        }
    }

    /**
     * 从系统配置加载 FORCE 档位验证渠道；facade 异常与配置值非法
     * （fromCode 抛 BizException）一并兜底回退 BOTH，避免阻断登录链路。
     */
    private TwoFactorChannelEnum loadForceChannel() {
        try {
            return TwoFactorChannelEnum.fromCode(
                    configFacade.getString(FORCE_CHANNEL_CONFIG_KEY, "BOTH"));
        } catch (Exception e) {
            // fromCode 对非法值抛 BizException，与 facade 异常一并兜底，避免阻断登录链路
            log.error("[双因素认证] 读取强制验证渠道失败，回退 BOTH", e);
            return TwoFactorChannelEnum.BOTH;
        }
    }
}
