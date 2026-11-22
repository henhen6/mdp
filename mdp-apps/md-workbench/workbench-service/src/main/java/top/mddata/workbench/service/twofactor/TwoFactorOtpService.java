package top.mddata.workbench.service.twofactor;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.BizException;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.workbench.TwoFactorCacheKeyBuilder;
import top.mddata.console.dto.message.MsgSendDto;
import top.mddata.console.dto.message.MsgSendMailDto;
import top.mddata.console.dto.message.MsgSendSmsDto;
import top.mddata.console.facade.message.MsgFacade;
import top.mddata.console.facade.system.ConfigFacade;

/**
 * 双因素认证 OTP 服务。
 *
 * <p>发送间隔 60 秒，验证码 5 分钟有效，校验失败累计 5 次后验证码即时作废，
 * 重新获取验证码时失败计数清零，每个验证码独立享有 5 次失败预算；
 * 校验成功立即删除防重放。场景（login / manage）与目标（手机号/邮箱）共同决定缓存键，
 * 杜绝跨场景、跨目标用码。失败计数由 {@link CacheOps#initAndIncr(CacheKey)} 原子完成
 * （裸字符串占位携带 TTL + 原生 INCR，固定窗口不续期），TTL 全部由
 * {@link TwoFactorCacheKeyBuilder} 工厂键携带，代码中不出现过期时间参数。</p>
 *
 * @author tangyh
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorOtpService {

    /** 验证码校验最大失败次数：达到后验证码作废，需重新获取 */
    private static final int MAX_FAIL_COUNT = 5;

    /** 易失流程键读取统一禁止空值回写，避免 GET miss 时 NullVal 污染计数键导致 INCR 失败 */
    private static final boolean NO_NULL_CACHE = false;

    /** 间隔键的占位值，仅用于 exists 判断，无业务含义 */
    private static final long LIMIT_MARKER = 1L;

    private static final String MOBILE_PATTERN = "^1\\d{10}$";

    /** 邮箱基本格式校验，仅作发送前格式兜底，不保证邮箱真实可用 */
    private static final String EMAIL_PATTERN = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";

    /** 短信模板标识的系统配置键（mdc_config.uniq_key，SECURITY 组） */
    private static final String SMS_TEMPLATE_KEY = "TWO_FACTOR_SMS_TEMPLATE";

    /** 短信模板标识缺省值：系统配置未录入该项时的兜底，对应控制台消息管理中的模板 */
    private static final String SMS_DEFAULT_TEMPLATE = "LOGIN_TWO_FACTOR_SMS";

    /** 邮件模板标识的系统配置键（mdc_config.uniq_key，SECURITY 组） */
    private static final String EMAIL_TEMPLATE_KEY = "TWO_FACTOR_EMAIL_TEMPLATE";

    /** 邮件模板标识缺省值：系统配置未录入该项时的兜底，对应控制台消息管理中的模板 */
    private static final String EMAIL_DEFAULT_TEMPLATE = "LOGIN_TWO_FACTOR_EMAIL";

    /** 忘记密码场景短信模板的系统配置键 */
    private static final String FORGET_SMS_TEMPLATE_KEY = "FORGET_PASSWORD_SMS_TEMPLATE";

    /** 忘记密码场景短信模板缺省值：对应控制台消息管理中的模板 */
    private static final String FORGET_SMS_DEFAULT_TEMPLATE = "FORGET_PASSWORD_SMS";

    /** 忘记密码场景邮件模板的系统配置键 */
    private static final String FORGET_EMAIL_TEMPLATE_KEY = "FORGET_PASSWORD_EMAIL_TEMPLATE";

    /** 忘记密码场景邮件模板缺省值：对应控制台消息管理中的模板 */
    private static final String FORGET_EMAIL_DEFAULT_TEMPLATE = "FORGET_PASSWORD_EMAIL";
    /** 邮件模板中的有效期参数（分钟），与 OTP 键 5 分钟 TTL 对应 */
    private static final String EMAIL_VALIDITY_MINUTES = "5";

    private final CacheOps cacheOps;
    private final ConfigFacade configFacade;
    private final MsgFacade msgFacade;

    /**
     * 发送 OTP。接收目标为手机号时走短信，其余按邮箱处理。
     * 写入新码的同时删除旧失败计数键，使新验证码重新获得完整的 5 次错误预算。
     *
     * @param scene  场景，取 {@link TwoFactorCacheKeyBuilder#SCENE_LOGIN} 等常量
     * @param target 接收目标（手机号或邮箱明文）
     * @return 脱敏后的接收目标
     */
    public String send(String scene, String target) {
        ArgumentAssert.isTrue(StrUtil.isNotBlank(scene), () -> new BizException("场景不能为空"));
        ArgumentAssert.isTrue(StrUtil.isNotBlank(target), () -> new BizException("接收目标不能为空"));
        // fail fast：目标格式不合法时不得触达缓存与消息通道，避免向畸形地址发短信或邮件
        ArgumentAssert.isTrue(
                target.matches(MOBILE_PATTERN) || target.matches(EMAIL_PATTERN),
                () -> new BizException("接收目标格式不正确"));

        CacheKey limitKey = TwoFactorCacheKeyBuilder.otpLimit(scene, target);
        if (Boolean.TRUE.equals(cacheOps.exists(limitKey))) {
            throw new BizException("验证码发送过于频繁，请稍后再试");
        }

        String code = RandomUtil.randomNumbers(6);
        cacheOps.set(TwoFactorCacheKeyBuilder.otp(scene, target), code);
        cacheOps.set(limitKey, LIMIT_MARKER);
        // 每个验证码拥有独立的 5 次失败预算：下发新码时清除旧码累计的失败计数，
        // 恢复“错误次数过多，请重新获取”的语义。此处先删后发，即便随后消息发送失败
        // 计数也已清空——该方向只会放宽限制，不会让用户被旧计数锁死，属可接受的安全取舍
        cacheOps.del(TwoFactorCacheKeyBuilder.otp(scene + ".fail", target));

        String masked;
        if (target.matches(MOBILE_PATTERN)) {
            MsgSendDto sms = MsgSendSmsDto.buildApiSender()
                    .addRecipient(target)
                    .setTemplateKey(resolveSmsTemplateKey(scene))
                    .addParam("code", code);
            msgFacade.sendByTemplateKey(sms);
            masked = DesensitizedUtil.mobilePhone(target);
        } else {
            MsgSendDto mail = MsgSendMailDto.buildApiSender()
                    .addRecipient(target)
                    .setTemplateKey(resolveEmailTemplateKey(scene))
                    .addParam("code", code)
                    .addParam("validityPeriod", EMAIL_VALIDITY_MINUTES);
            msgFacade.sendByTemplateKey(mail);
            masked = DesensitizedUtil.email(target);
        }
        // 只记录场景与脱敏目标，禁止打印验证码明文
        log.info("双因素验证码已发送 scene={}, target={}", scene, masked);
        return masked;
    }

    /**
     * 按场景解析短信模板标识。忘记密码场景使用专属模板键（文案可写"重置密码"），
     * 其余场景维持双因素模板键，已有配置不受影响。
     */
    private String resolveSmsTemplateKey(String scene) {
        return TwoFactorCacheKeyBuilder.SCENE_FORGET_PASSWORD.equals(scene)
                ? configFacade.getString(FORGET_SMS_TEMPLATE_KEY, FORGET_SMS_DEFAULT_TEMPLATE)
                : configFacade.getString(SMS_TEMPLATE_KEY, SMS_DEFAULT_TEMPLATE);
    }

    /**
     * 按场景解析邮件模板标识，策略同 {@link #resolveSmsTemplateKey(String)}。
     */
    private String resolveEmailTemplateKey(String scene) {
        return TwoFactorCacheKeyBuilder.SCENE_FORGET_PASSWORD.equals(scene)
                ? configFacade.getString(FORGET_EMAIL_TEMPLATE_KEY, FORGET_EMAIL_DEFAULT_TEMPLATE)
                : configFacade.getString(EMAIL_TEMPLATE_KEY, EMAIL_DEFAULT_TEMPLATE);
    }

    /**
     * 校验 OTP，成功立即删除防重放；失败累计 5 次作废验证码。
     *
     * @param scene  场景，取 {@link TwoFactorCacheKeyBuilder#SCENE_LOGIN} 等常量
     * @param target 发送时使用的接收目标
     * @param code   待校验验证码
     */
    public void verify(String scene, String target, String code) {
        ArgumentAssert.isTrue(StrUtil.isNotBlank(scene), () -> new BizException("场景不能为空"));
        ArgumentAssert.isTrue(StrUtil.isNotBlank(target), () -> new BizException("接收目标不能为空"));
        ArgumentAssert.isTrue(StrUtil.isNotBlank(code), () -> new BizException("验证码不能为空"));

        CacheKey otpKey = TwoFactorCacheKeyBuilder.otp(scene, target);
        String cached = cacheOps.<String>get(otpKey, NO_NULL_CACHE).getValue();
        if (cached == null) {
            throw new BizException("验证码已过期，请重新获取");
        }
        if (!cached.equals(code)) {
            recordFail(scene, target, otpKey);
        }
        cacheOps.del(otpKey);
    }

    /**
     * 记录一次验证码错误。原子自增失败计数，未达上限提示剩余次数，达到上限同步删除
     * OTP 键使验证码即时作废；计数键保留至 TTL 到期，持续拦截暴力尝试。
     */
    private void recordFail(String scene, String target, CacheKey otpKey) {
        CacheKey failKey = TwoFactorCacheKeyBuilder.otp(scene + ".fail", target);
        long next = cacheOps.initAndIncr(failKey);
        if (next >= MAX_FAIL_COUNT) {
            cacheOps.del(otpKey);
            throw new BizException("验证码错误次数过多，请重新获取");
        }
        throw new BizException("验证码错误，剩余 " + (MAX_FAIL_COUNT - next) + " 次机会");
    }
}
