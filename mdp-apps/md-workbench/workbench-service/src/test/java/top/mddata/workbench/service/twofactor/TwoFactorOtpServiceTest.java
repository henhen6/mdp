package top.mddata.workbench.service.twofactor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.exception.BizException;
import top.mddata.base.model.Kv;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.common.cache.workbench.TwoFactorCacheKeyBuilder;
import top.mddata.console.dto.message.MsgSendDto;
import top.mddata.console.dto.message.MsgSendMailDto;
import top.mddata.console.dto.message.MsgSendSmsDto;
import top.mddata.console.facade.message.MsgFacade;
import top.mddata.console.facade.system.ConfigFacade;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 2FA OTP 服务单元测试：60 秒发送间隔、短信/邮件通道、场景隔离、
 * initAndIncr 原子失败计数（5 次作废）、重新获取重置失败预算、
 * 目标格式校验、读键禁止空值回写。
 *
 * @author tangyh
 */
class TwoFactorOtpServiceTest {

    private static final String PHONE = "13800001111";
    private static final String EMAIL = "user@example.com";

    private CacheOps cacheOps;
    private ConfigFacade configFacade;
    private MsgFacade msgFacade;
    private TwoFactorOtpService otpService;

    @BeforeEach
    void setUp() {
        cacheOps = mock(CacheOps.class);
        configFacade = mock(ConfigFacade.class);
        msgFacade = mock(MsgFacade.class);
        otpService = new TwoFactorOtpService(cacheOps, configFacade, msgFacade);

        when(configFacade.getString("TWO_FACTOR_SMS_TEMPLATE", "LOGIN_TWO_FACTOR_SMS"))
                .thenReturn("LOGIN_TWO_FACTOR_SMS");
        when(configFacade.getString("TWO_FACTOR_EMAIL_TEMPLATE", "LOGIN_TWO_FACTOR_EMAIL"))
                .thenReturn("LOGIN_TWO_FACTOR_EMAIL");
    }

    @Test
    void send_shouldRejectWithin60Seconds() {
        // 60 秒间隔键存在即拒绝发送
        when(cacheOps.exists(any(CacheKey.class))).thenReturn(true);

        BizException e = assertThrows(BizException.class,
                () -> otpService.send(TwoFactorCacheKeyBuilder.SCENE_LOGIN, PHONE));
        assertEquals("验证码发送过于频繁，请稍后再试", e.getMessage());

        // 拒绝时不得写验证码、不得触发消息发送
        verify(cacheOps, never()).set(any(CacheKey.class), any());
        verify(msgFacade, never()).sendByTemplateKey(any());
    }

    @Test
    void send_shouldSendSmsCodeAndMaskPhone_whenIntervalOk() {
        when(cacheOps.exists(any(CacheKey.class))).thenReturn(false);

        String masked = otpService.send(TwoFactorCacheKeyBuilder.SCENE_LOGIN, PHONE);
        assertEquals("138****1111", masked);

        ArgumentCaptor<MsgSendDto> msgCaptor = ArgumentCaptor.forClass(MsgSendDto.class);
        verify(msgFacade).sendByTemplateKey(msgCaptor.capture());
        MsgSendSmsDto sms = (MsgSendSmsDto) msgCaptor.getValue();
        assertEquals(List.of(PHONE), sms.getRecipientList());
        assertEquals("LOGIN_TWO_FACTOR_SMS", sms.getTemplateKey());
        String code = findParam(sms, "code");
        assertTrue(code.matches("^\\d{6}$"), "验证码必须是 6 位数字");

        // 发送的验证码必须与写入 OTP 键的值一致，间隔键单独写入
        verify(cacheOps).set(argThat(this::isOtpKey), org.mockito.ArgumentMatchers.eq(code));
        verify(cacheOps).set(argThat(this::isLimitKey), any());
        // 下发新码必须同步删除旧失败计数键，保证每个验证码独立享有 5 次失败预算
        verify(cacheOps).del(varargKey(this::isFailKey));
    }

    @Test
    void send_shouldSendEmailAndMaskAddress_whenTargetIsEmail() {
        when(cacheOps.exists(any(CacheKey.class))).thenReturn(false);

        String masked = otpService.send(TwoFactorCacheKeyBuilder.SCENE_MANAGE, EMAIL);
        assertFalse(masked.contains(EMAIL), "脱敏结果不得回显完整邮箱");
        assertTrue(masked.startsWith("u") && masked.endsWith("@example.com")
                && masked.contains("*"), "邮箱脱敏格式异常: " + masked);

        ArgumentCaptor<MsgSendDto> msgCaptor = ArgumentCaptor.forClass(MsgSendDto.class);
        verify(msgFacade).sendByTemplateKey(msgCaptor.capture());
        MsgSendMailDto mail = (MsgSendMailDto) msgCaptor.getValue();
        assertEquals(List.of(EMAIL), mail.getRecipientList());
        assertEquals("LOGIN_TWO_FACTOR_EMAIL", mail.getTemplateKey());
        assertTrue(findParam(mail, "code").matches("^\\d{6}$"));
        assertEquals("5", findParam(mail, "validityPeriod"));
    }

    @Test
    void send_shouldFailFastWhenTargetBlank() {
        assertThrows(BizException.class,
                () -> otpService.send(TwoFactorCacheKeyBuilder.SCENE_LOGIN, " "));
        verifyNoInteractions(cacheOps);
        verifyNoInteractions(msgFacade);
    }

    @Test
    void send_shouldRejectMalformedTarget() {
        // 既不是手机号也不是邮箱的目标一律拒绝，且不得触达缓存与消息通道
        String[] invalidTargets = {"12345", "23800001111", "abc@xyz", "user@.com", "用户@example.com"};
        for (String target : invalidTargets) {
            BizException e = assertThrows(BizException.class,
                    () -> otpService.send(TwoFactorCacheKeyBuilder.SCENE_LOGIN, target));
            assertEquals("接收目标格式不正确", e.getMessage(), "非法目标未被拒绝: " + target);
        }
        verifyNoInteractions(cacheOps);
        verifyNoInteractions(msgFacade);
    }

    @Test
    void send_shouldResetFailBudget_whenNewOtpIssued() {
        // 旧码连续错误 4 次，失败计数累计到 4（尚未作废）
        stubOtpValue("654321");
        // 第 5 次返回值 1L 对应重新获取后新码的第 1 次错误，计数必须从 1 重新开始
        when(cacheOps.initAndIncr(any(CacheKey.class)))
                .thenReturn(1L, 2L, 3L, 4L, 1L);
        for (int next = 1; next <= 4; next++) {
            BizException e = assertThrows(BizException.class,
                    () -> otpService.verify(TwoFactorCacheKeyBuilder.SCENE_LOGIN, PHONE, "000000"));
            assertEquals("验证码错误，剩余 " + (5 - next) + " 次机会", e.getMessage());
        }

        // 间隔期过后重新获取验证码：旧失败计数键必须被删除
        when(cacheOps.exists(any(CacheKey.class))).thenReturn(false);
        assertDoesNotThrow(() -> otpService.send(TwoFactorCacheKeyBuilder.SCENE_LOGIN, PHONE));
        verify(cacheOps).del(varargKey(this::isFailKey));

        // 新码第 1 次错误提示剩余 4 次：若沿用旧计数会直接累计到 5 次作废
        BizException e = assertThrows(BizException.class,
                () -> otpService.verify(TwoFactorCacheKeyBuilder.SCENE_LOGIN, PHONE, "000000"));
        assertEquals("验证码错误，剩余 4 次机会", e.getMessage());
        // 计数仅 1 次，未达上限，验证码不得被作废
        verify(cacheOps, never()).del(varargKey(this::isOtpKey));
    }

    @Test
    void verify_shouldPassAndDeleteOtpOnce_whenCodeMatches() {
        // OTP 键有值，失败计数键无值
        stubOtpValue("123456");

        assertDoesNotThrow(() -> otpService.verify(
                TwoFactorCacheKeyBuilder.SCENE_LOGIN, PHONE, "123456"));

        // 校验成功立即删除 OTP 键防重放，且不触碰失败计数器
        verify(cacheOps).del(varargKey(this::isOtpKey));
        verify(cacheOps, never()).initAndIncr(any(CacheKey.class));
        // 读 OTP 键必须显式禁止空值回写，避免 miss 时 NullVal 污染后续 INCR
        verify(cacheOps).get(any(CacheKey.class), noNullCache());
    }

    @Test
    void verify_shouldBeIsolatedBetweenScenes() {
        // 只有 login 场景写了验证码，manage 场景的键读不到
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation -> {
            CacheKey key = invocation.getArgument(0);
            Object value = isLoginOtpKey(key.getKey()) ? "123456" : null;
            return new CacheResult<>(key.getKey(), value);
        });

        assertDoesNotThrow(() -> otpService.verify(
                TwoFactorCacheKeyBuilder.SCENE_LOGIN, PHONE, "123456"));
        BizException e = assertThrows(BizException.class, () -> otpService.verify(
                TwoFactorCacheKeyBuilder.SCENE_MANAGE, PHONE, "123456"));
        assertEquals("验证码已过期，请重新获取", e.getMessage());
    }

    @Test
    void verify_wrongCode_shouldCountFailuresAndInvalidateAtFive() {
        stubOtpValue("654321");
        // 真实 initAndIncr 首次裸字符串占位后自增，后续直接自增
        when(cacheOps.initAndIncr(any(CacheKey.class))).thenReturn(1L, 2L, 3L, 4L, 5L);

        for (int next = 1; next <= 4; next++) {
            BizException e = assertThrows(BizException.class,
                    () -> otpService.verify(TwoFactorCacheKeyBuilder.SCENE_LOGIN, PHONE, "000000"));
            assertEquals("验证码错误，剩余 " + (5 - next) + " 次机会", e.getMessage());
        }
        BizException last = assertThrows(BizException.class,
                () -> otpService.verify(TwoFactorCacheKeyBuilder.SCENE_LOGIN, PHONE, "000000"));
        assertEquals("验证码错误次数过多，请重新获取", last.getMessage());

        // 5 次失败只调原子计数原语，且作用于 fail 计数键；前 4 次保留验证码，第 5 次作废
        verify(cacheOps, org.mockito.Mockito.times(5))
                .initAndIncr(argThat(this::isFailKey));
        verify(cacheOps, org.mockito.Mockito.times(1)).del(varargKey(this::isOtpKey));
        // 计数器绝不允许走对象通道 set（序列化字节会让原生 INCR 报 value is not an integer）
        verify(cacheOps, never()).set(any(CacheKey.class), any());
    }

    @Test
    void verify_shouldThrowExpiredAndNeverCount_whenOtpMissing() {
        // 所有键 miss（返回不带载荷的 CacheResult）
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class)))
                .thenAnswer(invocation -> new CacheResult<>(
                        ((CacheKey) invocation.getArgument(0)).getKey()));

        BizException e = assertThrows(BizException.class,
                () -> otpService.verify(TwoFactorCacheKeyBuilder.SCENE_LOGIN, PHONE, "123456"));
        assertEquals("验证码已过期，请重新获取", e.getMessage());
        verify(cacheOps, never()).initAndIncr(any(CacheKey.class));
        verify(cacheOps, never()).del(any(CacheKey.class));
    }

    /**
     * OTP 键返回指定值，失败计数键等其他键一律 miss
     */
    private void stubOtpValue(String code) {
        when(cacheOps.get(any(CacheKey.class), any(boolean[].class))).thenAnswer(invocation -> {
            CacheKey key = invocation.getArgument(0);
            Object value = isLoginOtpKey(key.getKey()) ? code : null;
            return new CacheResult<>(key.getKey(), value);
        });
    }

    private String findParam(MsgSendDto dto, String name) {
        return dto.getParamList().stream()
                .filter(item -> name.equals(item.getKey()))
                .map(Kv::getValue)
                .findFirst()
                .orElseThrow(() -> new AssertionError("模板缺少参数: " + name));
    }

    private boolean isOtpKey(CacheKey key) {
        String redisKey = key.getKey();
        return redisKey.contains(".otp:") && !redisKey.contains(".fail.")
                && !redisKey.contains(".limit");
    }

    private boolean isLimitKey(CacheKey key) {
        return key.getKey().contains(".otp.limit:");
    }

    private boolean isFailKey(CacheKey key) {
        return key.getKey().contains(".fail.otp:");
    }

    private boolean isLoginOtpKey(String redisKey) {
        return redisKey.contains(":login.otp:");
    }

    /**
     * 契约匹配器：get 的 cacheNullValues 可变参数必须且只含 false，
     * 禁止 GET miss 时框架向 OTP 键/计数键回写 NullVal 占位。
     */
    private static final class NoNullCacheMatcher implements ArgumentMatcher<boolean[]> {
        @Override
        public boolean matches(boolean[] arr) {
            return arr != null && arr.length == 1 && !arr[0];
        }

        @Override
        public Class<?> type() {
            return boolean[].class;
        }
    }

    private static boolean[] noNullCache() {
        return argThat(new NoNullCacheMatcher());
    }

    /**
     * del(CacheKey...) 是可变参数重载，自定义匹配器须按整个 vararg 数组匹配
     * （重写 type() 声明数组类型），单元素匹配器对单元素 vararg 调用不会生效。
     */
    private CacheKey[] varargKey(ArgumentMatcher<CacheKey> elementMatcher) {
        return argThat(new ArgumentMatcher<>() {
            @Override
            public boolean matches(CacheKey[] keys) {
                return keys != null && keys.length == 1 && elementMatcher.matches(keys[0]);
            }

            @Override
            public Class<?> type() {
                return CacheKey[].class;
            }
        });
    }
}
