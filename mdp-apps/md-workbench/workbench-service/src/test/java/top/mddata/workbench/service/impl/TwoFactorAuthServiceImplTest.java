package top.mddata.workbench.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import top.mddata.base.exception.BizException;
import top.mddata.common.entity.User;
import top.mddata.workbench.dto.TwoFactorDisableDto;
import top.mddata.workbench.dto.TwoFactorEnableDto;
import top.mddata.workbench.dto.TwoFactorSendDto;
import top.mddata.workbench.dto.TwoFactorVerifyDto;
import top.mddata.workbench.entity.UserTwoFactorAuth;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.TwoFactorChannelEnum;
import top.mddata.workbench.enumeration.TwoFactorStrategyEnum;
import top.mddata.workbench.mapper.UserTwoFactorAuthMapper;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.service.TwoFactorOtpService;
import top.mddata.workbench.service.TwoFactorStrategyResolver;
import top.mddata.workbench.service.TwoFactorTicketService;
import top.mddata.workbench.service.TwoFactorTicketService.TwoFactorTicketPayload;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.TwoFactorManageSendVo;
import top.mddata.workbench.vo.TwoFactorStatusVo;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 双因素核心服务测试：触发判定矩阵（spec 3.2）、票据两段式校验、
 * 开通/关闭/换渠道、upsert 幂等、status 策略分支、FORCE 登录实时换渠道。
 *
 * @author tangyh
 * @since 2026-11-16
 */
class TwoFactorAuthServiceImplTest {

    private static final String TICKET = "ticket-1";

    private UserTwoFactorAuthMapper mapper;
    private TwoFactorStrategyResolver strategyResolver;
    private TwoFactorTicketService ticketService;
    private TwoFactorOtpService otpService;
    private SsoUserService ssoUserService;
    private TwoFactorAuthServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(UserTwoFactorAuthMapper.class);
        strategyResolver = mock(TwoFactorStrategyResolver.class);
        ticketService = mock(TwoFactorTicketService.class);
        otpService = mock(TwoFactorOtpService.class);
        ssoUserService = mock(SsoUserService.class);
        service = new TwoFactorAuthServiceImpl(mapper, strategyResolver,
                ticketService, otpService, ssoUserService);
        // FORCE 强制渠道默认打桩 BOTH，与生产默认配置一致，保持多数 FORCE 既有用例口径
        when(strategyResolver.resolveForceChannel()).thenReturn(TwoFactorChannelEnum.BOTH);
    }

    // ==================== 触发判定矩阵 ====================

    private User user(boolean enabledRecord) {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPhone("13800001111");
        if (enabledRecord) {
            when(mapper.selectOneByQuery(any())).thenReturn(twoFactorRecord(1L, true, "BOTH"));
        } else {
            when(mapper.selectOneByQuery(any())).thenReturn(null);
        }
        return user;
    }

    @Test
    void strategyOff_neverTrigger() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OFF);

        assertFalse(service.shouldTrigger(user(true)));
        assertFalse(service.shouldTrigger(user(false)));
        // OFF 时策略短路，不得查库
        verifyNoInteractions(mapper);
    }

    @Test
    void strategyOptional_triggersOnlyEnabledUser() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OPTIONAL);

        assertTrue(service.shouldTrigger(user(true)));
        assertFalse(service.shouldTrigger(user(false)));
    }

    @Test
    void strategyOptional_disabledRecordNotTrigger() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OPTIONAL);
        when(mapper.selectOneByQuery(any()))
                .thenReturn(twoFactorRecord(1L, false, "PHONE"));

        assertFalse(service.shouldTrigger(user(false)));
    }

    @Test
    void strategyForce_triggersEnabledUser() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);

        assertTrue(service.shouldTrigger(user(true)));
    }

    @Test
    void strategyForce_triggersUnenabledUserWithChannel() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);

        assertTrue(service.shouldTrigger(user(false)));
    }

    // ==================== startTwoFactor ====================

    @Test
    void startTwoFactor_optionalEnabledBothUser_issuesTicketAndReturnsMaskedVo() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OPTIONAL);
        User user = fullUser();
        when(mapper.selectOneByQuery(any())).thenReturn(twoFactorRecord(1L, true, "BOTH"));
        when(ticketService.issue(any())).thenReturn(TICKET);

        LoginVo vo = service.startTwoFactor(user, AuthTypeEnum.USERNAME, "admin",
                "device-1", "chrome");

        assertEquals("TWO_FACTOR", vo.getStatus());
        assertEquals(TICKET, vo.getTwoFactorTicket());
        assertEquals("BOTH", vo.getTwoFactorChannel());
        assertEquals("138****1111", vo.getTwoFactorMaskedTarget());

        ArgumentCaptor<TwoFactorTicketPayload> captor =
                ArgumentCaptor.forClass(TwoFactorTicketPayload.class);
        verify(ticketService).issue(captor.capture());
        TwoFactorTicketPayload payload = captor.getValue();
        assertEquals(1L, payload.getUserId());
        assertEquals("USERNAME", payload.getAuthType());
        assertEquals("admin", payload.getAccount());
        assertEquals("device-1", payload.getDeviceId());
        assertEquals("chrome", payload.getDeviceInfo());
        // 开通渠道固定为 BOTH；两渠道齐时 BOTH 自适应投递手机
        assertEquals("BOTH", payload.getOpenedChannel());
        assertEquals("PHONE", payload.getChannel());
        assertEquals("13800001111", payload.getTarget());
        assertEquals("13800001111", payload.getPhone());
        assertEquals("admin@example.com", payload.getEmail());
    }

    @Test
    void startTwoFactor_optionalEnabledPhoneUser_openedPhone() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OPTIONAL);
        User user = fullUser();
        when(mapper.selectOneByQuery(any())).thenReturn(twoFactorRecord(1L, true, "PHONE"));
        when(ticketService.issue(any())).thenReturn(TICKET);

        LoginVo vo = service.startTwoFactor(user, AuthTypeEnum.USERNAME, "admin", null, null);

        // OPTIONAL 取用户开通记录的自选单渠道，即使两渠道均已绑定也不放开为 BOTH
        assertEquals("PHONE", vo.getTwoFactorChannel());
        ArgumentCaptor<TwoFactorTicketPayload> captor =
                ArgumentCaptor.forClass(TwoFactorTicketPayload.class);
        verify(ticketService).issue(captor.capture());
        assertEquals("PHONE", captor.getValue().getOpenedChannel());
        assertEquals("PHONE", captor.getValue().getChannel());
    }

    @Test
    void startTwoFactor_forceConfigBoth_bothBound_ignoresRecordAndOpenedBoth() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        when(ticketService.issue(any())).thenReturn(TICKET);

        LoginVo vo = service.startTwoFactor(fullUser(), AuthTypeEnum.USERNAME, "admin",
                null, null);

        // FORCE 渠道取强制渠道配置（setUp 默认 BOTH）：忽略用户记录，两渠道齐 opened=BOTH，
        // 前端可达“换一种方式”
        assertEquals("BOTH", vo.getTwoFactorChannel());
        assertEquals("138****1111", vo.getTwoFactorMaskedTarget());
        ArgumentCaptor<TwoFactorTicketPayload> captor =
                ArgumentCaptor.forClass(TwoFactorTicketPayload.class);
        verify(ticketService).issue(captor.capture());
        assertEquals("BOTH", captor.getValue().getOpenedChannel());
        // 两渠道齐：BOTH 自适应投递手机
        assertEquals("PHONE", captor.getValue().getChannel());
        assertEquals("13800001111", captor.getValue().getTarget());
        // FORCE 短路：不读用户开通记录
        verify(mapper, never()).selectOneByQuery(any());
    }

    @Test
    void startTwoFactor_forceConfigPhone_phoneBound_openedPhone() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        when(strategyResolver.resolveForceChannel())
                .thenReturn(TwoFactorChannelEnum.PHONE);
        User user = new User();
        user.setId(2L);
        user.setPhone("13800001111");
        user.setEmail("user2@example.com");
        when(ticketService.issue(any())).thenReturn(TICKET);

        LoginVo vo = service.startTwoFactor(user, AuthTypeEnum.USERNAME, "13800001111",
                null, null);

        // 配置指定手机：即使邮箱也已绑定，opened 仍为 PHONE，按手机投递，不允许降级到邮箱
        assertEquals("PHONE", vo.getTwoFactorChannel());
        assertEquals("138****1111", vo.getTwoFactorMaskedTarget());
        ArgumentCaptor<TwoFactorTicketPayload> captor =
                ArgumentCaptor.forClass(TwoFactorTicketPayload.class);
        verify(ticketService).issue(captor.capture());
        assertEquals("PHONE", captor.getValue().getOpenedChannel());
        assertEquals("PHONE", captor.getValue().getChannel());
        assertEquals("13800001111", captor.getValue().getTarget());
        verify(mapper, never()).selectOneByQuery(any());
    }

    @Test
    void startTwoFactor_forceConfigPhone_phoneMissing_throwsPhoneMessageAndNoTicket() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        when(strategyResolver.resolveForceChannel())
                .thenReturn(TwoFactorChannelEnum.PHONE);
        // 仅绑定邮箱：严格拦截，不自动降级到邮箱
        User user = new User();
        user.setId(3L);
        user.setEmail("user3@example.com");

        BizException ex = assertThrows(BizException.class,
                () -> service.startTwoFactor(user, AuthTypeEnum.EMAIL, "user3@example.com",
                        null, null));
        assertEquals("系统指定验证渠道为手机，请绑定手机号后登录，或联系管理员", ex.getMessage());
        verify(ticketService, never()).issue(any());
        verify(mapper, never()).selectOneByQuery(any());
    }

    @Test
    void startTwoFactor_forceConfigEmail_emailBound_openedEmail() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        when(strategyResolver.resolveForceChannel())
                .thenReturn(TwoFactorChannelEnum.EMAIL);
        User user = new User();
        user.setId(4L);
        user.setEmail("user4@example.com");
        when(ticketService.issue(any())).thenReturn(TICKET);

        LoginVo vo = service.startTwoFactor(user, AuthTypeEnum.EMAIL, "user4@example.com",
                null, null);

        assertEquals("EMAIL", vo.getTwoFactorChannel());
        assertEquals("u****@example.com", vo.getTwoFactorMaskedTarget());
        ArgumentCaptor<TwoFactorTicketPayload> captor =
                ArgumentCaptor.forClass(TwoFactorTicketPayload.class);
        verify(ticketService).issue(captor.capture());
        assertEquals("EMAIL", captor.getValue().getOpenedChannel());
        assertEquals("EMAIL", captor.getValue().getChannel());
        assertEquals("user4@example.com", captor.getValue().getTarget());
        verify(mapper, never()).selectOneByQuery(any());
    }

    @Test
    void startTwoFactor_forceConfigEmail_emailMissing_throwsEmailMessageAndNoTicket() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        when(strategyResolver.resolveForceChannel())
                .thenReturn(TwoFactorChannelEnum.EMAIL);
        // 仅绑定手机：严格拦截，不自动降级到手机
        User user = new User();
        user.setId(5L);
        user.setPhone("13800001111");

        BizException ex = assertThrows(BizException.class,
                () -> service.startTwoFactor(user, AuthTypeEnum.USERNAME, "13800001111",
                        null, null));
        assertEquals("系统指定验证渠道为邮箱，请绑定邮箱后登录，或联系管理员", ex.getMessage());
        verify(ticketService, never()).issue(any());
        verify(mapper, never()).selectOneByQuery(any());
    }

    @Test
    void startTwoFactor_forceConfigBoth_neitherBound_throwsAndNoTicket() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        User user = new User();
        user.setId(6L);

        BizException ex = assertThrows(BizException.class,
                () -> service.startTwoFactor(user, AuthTypeEnum.USERNAME, "nouser", null, null));
        assertEquals("双因素认证渠道未绑定手机或邮箱，请联系管理员", ex.getMessage());
        verify(ticketService, never()).issue(any());
        verify(mapper, never()).selectOneByQuery(any());
    }

    @Test
    void startTwoFactor_forceConfigBoth_emailOnly_deliversEmailAndIssuesTicket() {
        // BOTH 语义为“手机或邮箱均可”：仅绑邮箱时投递渠道自适应为 EMAIL，签票成功
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        User user = new User();
        user.setId(7L);
        user.setEmail("user7@example.com");
        when(ticketService.issue(any())).thenReturn(TICKET);

        LoginVo vo = service.startTwoFactor(user, AuthTypeEnum.EMAIL, "user7@example.com",
                null, null);

        // 开通渠道仍回传 BOTH，前端可达“换一种方式”；脱敏目标为邮箱
        assertEquals("TWO_FACTOR", vo.getStatus());
        assertEquals(TICKET, vo.getTwoFactorTicket());
        assertEquals("BOTH", vo.getTwoFactorChannel());
        assertEquals("u****@example.com", vo.getTwoFactorMaskedTarget());
        ArgumentCaptor<TwoFactorTicketPayload> captor =
                ArgumentCaptor.forClass(TwoFactorTicketPayload.class);
        verify(ticketService).issue(captor.capture());
        assertEquals("BOTH", captor.getValue().getOpenedChannel());
        assertEquals("EMAIL", captor.getValue().getChannel());
        assertEquals("user7@example.com", captor.getValue().getTarget());
        assertNull(captor.getValue().getPhone());
        assertEquals("user7@example.com", captor.getValue().getEmail());
        verify(mapper, never()).selectOneByQuery(any());
    }

    @Test
    void startTwoFactor_forceConfigBoth_phoneOnly_deliversPhoneAndIssuesTicket() {
        // BOTH 自适应：仅绑手机时投递 PHONE
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        User user = new User();
        user.setId(8L);
        user.setPhone("13800008888");
        when(ticketService.issue(any())).thenReturn(TICKET);

        LoginVo vo = service.startTwoFactor(user, AuthTypeEnum.USERNAME, "13800008888",
                null, null);

        assertEquals("BOTH", vo.getTwoFactorChannel());
        assertEquals("138****8888", vo.getTwoFactorMaskedTarget());
        ArgumentCaptor<TwoFactorTicketPayload> captor =
                ArgumentCaptor.forClass(TwoFactorTicketPayload.class);
        verify(ticketService).issue(captor.capture());
        assertEquals("BOTH", captor.getValue().getOpenedChannel());
        assertEquals("PHONE", captor.getValue().getChannel());
        assertEquals("13800008888", captor.getValue().getTarget());
        assertNull(captor.getValue().getEmail());
        verify(mapper, never()).selectOneByQuery(any());
    }

    @Test
    void startTwoFactor_forceConfigBoth_emailOnly_thenSendWithoutChannel_sendsEmail() {
        // 端到端对称：2FA 页挂载即不带渠道发首条验证码，BOTH 仅邮箱票据必须发往邮箱，
        // 不能因“BOTH 手机优先”把无手机用户重新打回 PHONE 拦截
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        User user = new User();
        user.setId(7L);
        user.setEmail("user7@example.com");
        when(ticketService.issue(any())).thenReturn(TICKET);

        LoginVo vo = service.startTwoFactor(user, AuthTypeEnum.EMAIL, "user7@example.com",
                null, null);
        assertEquals("BOTH", vo.getTwoFactorChannel());

        TwoFactorTicketPayload issued = new TwoFactorTicketPayload();
        issued.setUserId(7L);
        issued.setOpenedChannel("BOTH");
        issued.setChannel("EMAIL");
        issued.setTarget("user7@example.com");
        issued.setEmail("user7@example.com");
        when(ticketService.peek(TICKET)).thenReturn(issued);
        when(otpService.send(eq("login"), eq("user7@example.com")))
                .thenReturn("u****@example.com");

        TwoFactorSendDto dto = new TwoFactorSendDto();
        dto.setTicket(TICKET);
        String masked = service.sendLoginOtp(dto);

        assertEquals("u****@example.com", masked);
        verify(otpService).send("login", "user7@example.com");
        // 默认发送沿用票据快照，不查库
        verify(ssoUserService, never()).getById(any());
        ArgumentCaptor<TwoFactorTicketPayload> captor =
                ArgumentCaptor.forClass(TwoFactorTicketPayload.class);
        verify(ticketService).updatePayload(eq(TICKET), captor.capture());
        assertEquals("EMAIL", captor.getValue().getChannel());
        assertEquals("user7@example.com", captor.getValue().getTarget());
    }

    // ==================== sendLoginOtp ====================

    @Test
    void sendLoginOtp_bothChannelSwitchEmail_reloadsBindingAndSends() {
        TwoFactorTicketPayload payload = loginPayload("BOTH", "13800001111",
                "admin@example.com");
        when(ticketService.peek(TICKET)).thenReturn(payload);
        when(ssoUserService.getById(1L)).thenReturn(fullUser());
        when(otpService.send(eq("login"), eq("admin@example.com")))
                .thenReturn("a***@example.com");

        TwoFactorSendDto dto = new TwoFactorSendDto();
        dto.setTicket(TICKET);
        dto.setChannel("EMAIL");

        String masked = service.sendLoginOtp(dto);

        assertEquals("a***@example.com", masked);
        // 换渠道必须实时重查绑定，不能沿用票据快照
        verify(ssoUserService).getById(1L);
        ArgumentCaptor<TwoFactorTicketPayload> captor =
                ArgumentCaptor.forClass(TwoFactorTicketPayload.class);
        verify(ticketService).updatePayload(eq(TICKET), captor.capture());
        assertEquals("EMAIL", captor.getValue().getChannel());
        assertEquals("admin@example.com", captor.getValue().getTarget());
        // 切换投递渠道不得改变开通渠道
        assertEquals("BOTH", captor.getValue().getOpenedChannel());
    }

    @Test
    void sendLoginOtp_bothChannelSwitchTwice_bothSucceedAndOpenedChannelKept() {
        // 回归：BOTH 用户 PHONE→EMAIL→PHONE 第二次切换曾因投递渠道被误当开通渠道而拒绝
        TwoFactorTicketPayload payload = loginPayload("BOTH", "13800001111",
                "admin@example.com");
        // 模拟 Redis 读写：每次 peek/回写都是序列化快照，避免同一可变实例掩盖渠道变迁
        AtomicReference<TwoFactorTicketPayload> stored =
                new AtomicReference<>(copyPayload(payload));
        when(ticketService.peek(TICKET))
                .thenAnswer(invocation -> copyPayload(stored.get()));
        doAnswer(invocation -> {
            stored.set(copyPayload(invocation.getArgument(1)));
            return null;
        }).when(ticketService).updatePayload(eq(TICKET), any());
        // 两次切换都必须实时重查绑定
        when(ssoUserService.getById(1L)).thenReturn(fullUser());
        when(otpService.send(eq("login"), eq("admin@example.com")))
                .thenReturn("a***@example.com");
        when(otpService.send(eq("login"), eq("13800001111")))
                .thenReturn("138****1111");

        String emailMasked = service.sendLoginOtp(sendDto("EMAIL"));
        String phoneMasked = service.sendLoginOtp(sendDto("PHONE"));

        assertEquals("a***@example.com", emailMasked);
        assertEquals("138****1111", phoneMasked);
        ArgumentCaptor<TwoFactorTicketPayload> captor =
                ArgumentCaptor.forClass(TwoFactorTicketPayload.class);
        verify(ticketService, times(2)).updatePayload(eq(TICKET), captor.capture());
        assertEquals("EMAIL", captor.getAllValues().get(0).getChannel());
        assertEquals("admin@example.com", captor.getAllValues().get(0).getTarget());
        assertEquals("PHONE", captor.getAllValues().get(1).getChannel());
        assertEquals("13800001111", captor.getAllValues().get(1).getTarget());
        // 开通渠道两次切换后仍为 BOTH
        assertEquals("BOTH", captor.getAllValues().get(0).getOpenedChannel());
        assertEquals("BOTH", captor.getAllValues().get(1).getOpenedChannel());
        verify(otpService).send("login", "admin@example.com");
        verify(otpService).send("login", "13800001111");
        verify(ssoUserService, times(2)).getById(1L);
    }

    @Test
    void sendLoginOtp_withoutChannel_usesPhoneFirst() {
        TwoFactorTicketPayload payload = loginPayload("BOTH", "13800001111",
                "admin@example.com");
        when(ticketService.peek(TICKET)).thenReturn(payload);
        when(otpService.send(eq("login"), eq("13800001111"))).thenReturn("138****1111");

        TwoFactorSendDto dto = new TwoFactorSendDto();
        dto.setTicket(TICKET);

        String masked = service.sendLoginOtp(dto);

        assertEquals("138****1111", masked);
        verify(ticketService).updatePayload(TICKET, payload);
        // 默认发送沿用票据快照，不查库
        verify(ssoUserService, never()).getById(any());
    }

    @Test
    void sendLoginOtp_invalidTicket_throwsAndNoSend() {
        when(ticketService.peek(TICKET)).thenReturn(null);

        TwoFactorSendDto dto = new TwoFactorSendDto();
        dto.setTicket(TICKET);

        BizException ex = assertThrows(BizException.class, () -> service.sendLoginOtp(dto));
        assertTrue(ex.getMessage().contains("票据无效或已过期"));
        verify(otpService, never()).send(any(), any());
        verify(ticketService, never()).updatePayload(any(), any());
    }

    @Test
    void sendLoginOtp_optionalPhoneChannel_switchEmailRejected() {
        // OPTIONAL 已开通单渠道用户保持原语义：用户自选不被覆盖，即使两渠道均已绑定
        // （opened=PHONE 单渠道即拒绝，与策略无关，send 阶段不再读策略）
        TwoFactorTicketPayload payload = loginPayload("PHONE", "13800001111",
                "admin@example.com");
        when(ticketService.peek(TICKET)).thenReturn(payload);

        assertThrows(BizException.class, () -> service.sendLoginOtp(sendDto("EMAIL")));
        verify(otpService, never()).send(any(), any());
        verify(ticketService, never()).updatePayload(any(), any());
        // 切换校验先于绑定重查，拒绝时不得查库
        verify(ssoUserService, never()).getById(any());
        verifyNoInteractions(strategyResolver);
    }

    @Test
    void startTwoFactor_forceBothBound_thenSendSwitchEmailSucceeds() {
        // 回归 C-1：经 startTwoFactor 真实路径，FORCE 两渠道齐用户 opened=BOTH，
        // 第二步“换一种方式”切 EMAIL 必须可达且成功
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        AtomicReference<TwoFactorTicketPayload> stored = new AtomicReference<>();
        when(ticketService.issue(any())).thenAnswer(invocation -> {
            stored.set(copyPayload(invocation.getArgument(0)));
            return TICKET;
        });
        when(ticketService.peek(TICKET)).thenAnswer(invocation -> copyPayload(stored.get()));
        doAnswer(invocation -> {
            stored.set(copyPayload(invocation.getArgument(1)));
            return null;
        }).when(ticketService).updatePayload(eq(TICKET), any());
        when(ssoUserService.getById(1L)).thenReturn(fullUser());
        when(otpService.send(eq("login"), eq("admin@example.com")))
                .thenReturn("a***@example.com");

        LoginVo vo = service.startTwoFactor(fullUser(), AuthTypeEnum.USERNAME, "admin",
                null, null);
        assertEquals("BOTH", vo.getTwoFactorChannel());

        String masked = service.sendLoginOtp(sendDto("EMAIL"));

        assertEquals("a***@example.com", masked);
        assertEquals("EMAIL", stored.get().getChannel());
        assertEquals("admin@example.com", stored.get().getTarget());
        // 切换投递渠道不得改变开通渠道
        assertEquals("BOTH", stored.get().getOpenedChannel());
    }

    @Test
    void sendLoginOtp_forcePhoneOnly_switchEmailRejected() {
        // FORCE 仅手机用户 opened=PHONE：单渠道不允许切换，避免切到无目标渠道
        TwoFactorTicketPayload payload = loginPayload("PHONE", "13800001111", null);
        when(ticketService.peek(TICKET)).thenReturn(payload);

        assertThrows(BizException.class, () -> service.sendLoginOtp(sendDto("EMAIL")));
        verify(otpService, never()).send(any(), any());
        verify(ticketService, never()).updatePayload(any(), any());
        verify(ssoUserService, never()).getById(any());
    }

    @Test
    void sendLoginOtp_switchChannel_emailUnboundAfterTicket_throws() {
        // I-1：票据签发时冻结了邮箱，窗口期内邮箱被解绑；换渠道实时重查后必须拒绝，
        // 不能把 OTP 发往快照中的旧邮箱
        TwoFactorTicketPayload payload = loginPayload("BOTH", "13800001111",
                "admin@example.com");
        when(ticketService.peek(TICKET)).thenReturn(payload);
        User phoneOnly = new User();
        phoneOnly.setId(1L);
        phoneOnly.setPhone("13800001111");
        when(ssoUserService.getById(1L)).thenReturn(phoneOnly);

        BizException ex = assertThrows(BizException.class,
                () -> service.sendLoginOtp(sendDto("EMAIL")));
        assertTrue(ex.getMessage().contains("该渠道未绑定接收目标"));
        verify(otpService, never()).send(any(), any());
        verify(ticketService, never()).updatePayload(any(), any());
    }

    @Test
    void sendLoginOtp_switchChannel_emailReboundAfterTicket_sendsToNewEmail() {
        // I-1：窗口期内换绑新邮箱，换渠道必须发往实时目标而非票据冻结的旧邮箱
        TwoFactorTicketPayload payload = loginPayload("BOTH", "13800001111",
                "old@example.com");
        when(ticketService.peek(TICKET)).thenReturn(payload);
        User rebound = new User();
        rebound.setId(1L);
        rebound.setPhone("13800001111");
        rebound.setEmail("new@example.com");
        when(ssoUserService.getById(1L)).thenReturn(rebound);
        when(otpService.send(eq("login"), eq("new@example.com")))
                .thenReturn("n***@example.com");

        String masked = service.sendLoginOtp(sendDto("EMAIL"));

        assertEquals("n***@example.com", masked);
        verify(otpService, never()).send(eq("login"), eq("old@example.com"));
        ArgumentCaptor<TwoFactorTicketPayload> captor =
                ArgumentCaptor.forClass(TwoFactorTicketPayload.class);
        verify(ticketService).updatePayload(eq(TICKET), captor.capture());
        assertEquals("new@example.com", captor.getValue().getTarget());
        // 载荷绑定快照同步刷新为实时值
        assertEquals("new@example.com", captor.getValue().getEmail());
    }

    @Test
    void sendLoginOtp_channelTargetMissing_throwsAndNoSend() {
        TwoFactorTicketPayload payload = loginPayload("PHONE", null, null);
        when(ticketService.peek(TICKET)).thenReturn(payload);

        TwoFactorSendDto dto = new TwoFactorSendDto();
        dto.setTicket(TICKET);

        BizException ex = assertThrows(BizException.class, () -> service.sendLoginOtp(dto));
        assertTrue(ex.getMessage().contains("该渠道未绑定接收目标"));
        verify(otpService, never()).send(any(), any());
    }

    // ==================== verifyLogin 两段式 ====================

    @Test
    void verifyLogin_invalidTicket_throwsWithoutVerify() {
        when(ticketService.peek(TICKET)).thenReturn(null);

        TwoFactorVerifyDto dto = verifyDto("123456");

        BizException ex = assertThrows(BizException.class, () -> service.verifyLogin(dto));
        assertTrue(ex.getMessage().contains("票据无效或已过期"));
        verify(otpService, never()).verify(any(), any(), any());
        verify(ticketService, never()).consume(any());
        verify(ticketService, never()).recordFail(any());
    }

    @Test
    void verifyLogin_otpFail_recordsFailAndKeepsTicket() {
        TwoFactorTicketPayload payload = loginPayload("PHONE", "13800001111", null);
        when(ticketService.peek(TICKET)).thenReturn(payload);
        doThrow(new BizException("验证码错误，剩余 4 次机会"))
                .when(otpService).verify("login", "13800001111", "000000");

        BizException ex = assertThrows(BizException.class,
                () -> service.verifyLogin(verifyDto("000000")));
        assertEquals("验证码错误，剩余 4 次机会", ex.getMessage());
        // 失败必须累计票据失败次数，且票据不能被消费
        verify(ticketService).recordFail(TICKET);
        verify(ticketService, never()).consume(any());
    }

    @Test
    void verifyLogin_recordFailThrows_propagatesWithoutConsume() {
        TwoFactorTicketPayload payload = loginPayload("PHONE", "13800001111", null);
        when(ticketService.peek(TICKET)).thenReturn(payload);
        doThrow(new BizException("验证码错误，剩余 4 次机会"))
                .when(otpService).verify(any(), any(), any());
        doThrow(new BizException("验证失败次数过多，请重新登录"))
                .when(ticketService).recordFail(TICKET);

        BizException ex = assertThrows(BizException.class,
                () -> service.verifyLogin(verifyDto("000000")));
        assertEquals("验证失败次数过多，请重新登录", ex.getMessage());
        verify(ticketService, never()).consume(any());
    }

    @Test
    void verifyLogin_success_verifiesBeforeConsume() {
        TwoFactorTicketPayload payload = loginPayload("PHONE", "13800001111", null);
        when(ticketService.peek(TICKET)).thenReturn(payload);
        when(ticketService.consume(TICKET)).thenReturn(payload);

        TwoFactorTicketPayload result = service.verifyLogin(verifyDto("123456"));

        assertSame(payload, result);
        // 顺序约束：OTP 校验成功后才允许消费票据
        InOrder inOrder = inOrder(otpService, ticketService);
        inOrder.verify(otpService).verify("login", "13800001111", "123456");
        inOrder.verify(ticketService).consume(TICKET);
        verify(ticketService, never()).recordFail(any());
    }

    @Test
    void verifyLogin_concurrentConsumed_throws() {
        TwoFactorTicketPayload payload = loginPayload("PHONE", "13800001111", null);
        when(ticketService.peek(TICKET)).thenReturn(payload);
        when(ticketService.consume(TICKET)).thenReturn(null);

        assertThrows(BizException.class, () -> service.verifyLogin(verifyDto("123456")));
    }

    // ==================== status ====================

    @Test
    void status_off_ignoresRecordAndReturnsDisabled() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OFF);

        TwoFactorStatusVo vo = service.status(1L);

        assertEquals(TwoFactorStrategyEnum.OFF, vo.getStrategy());
        assertFalse(vo.getEnabled());
        assertNull(vo.getChannel());
        assertNull(vo.getMaskedTarget());
        // 功能关闭策略短路：不读用户记录，也不查用户
        verify(mapper, never()).selectOneByQuery(any());
        verifyNoInteractions(ssoUserService);
    }

    @Test
    void status_forceConfigBoth_bothBound_returnsBothEnabledAndPhoneMasked() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        when(ssoUserService.getById(1L)).thenReturn(fullUser());

        TwoFactorStatusVo vo = service.status(1L);

        assertEquals(TwoFactorStrategyEnum.FORCE, vo.getStrategy());
        assertTrue(vo.getEnabled());
        // 渠道回显强制渠道配置（setUp 默认 BOTH），非按绑定自适应
        assertEquals(TwoFactorChannelEnum.BOTH, vo.getChannel());
        // BOTH 默认投递渠道为手机，脱敏手机
        assertEquals("138****1111", vo.getMaskedTarget());
        // FORCE 忽略用户开通记录
        verify(mapper, never()).selectOneByQuery(any());
    }

    @Test
    void status_forceConfigPhone_phoneBound_returnsPhoneEnabled() {
        User user = new User();
        user.setId(1L);
        user.setPhone("13800001111");
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        when(strategyResolver.resolveForceChannel())
                .thenReturn(TwoFactorChannelEnum.PHONE);
        when(ssoUserService.getById(1L)).thenReturn(user);

        TwoFactorStatusVo vo = service.status(1L);

        assertTrue(vo.getEnabled());
        assertEquals(TwoFactorChannelEnum.PHONE, vo.getChannel());
        assertEquals("138****1111", vo.getMaskedTarget());
        verify(mapper, never()).selectOneByQuery(any());
    }

    @Test
    void status_forceConfigPhone_phoneMissing_returnsPhoneWithoutMaskedTarget() {
        // 配置指定手机但用户只绑邮箱：渠道仍回显 PHONE，缺目标时 maskedTarget=null
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        when(strategyResolver.resolveForceChannel())
                .thenReturn(TwoFactorChannelEnum.PHONE);
        when(ssoUserService.getById(1L)).thenReturn(user);

        TwoFactorStatusVo vo = service.status(1L);

        assertTrue(vo.getEnabled());
        assertEquals(TwoFactorChannelEnum.PHONE, vo.getChannel());
        assertNull(vo.getMaskedTarget());
        verify(mapper, never()).selectOneByQuery(any());
    }

    @Test
    void status_forceConfigBoth_neitherBound_returnsBothWithoutMaskedTarget() {
        User user = new User();
        user.setId(1L);
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        when(ssoUserService.getById(1L)).thenReturn(user);

        TwoFactorStatusVo vo = service.status(1L);

        // 配置 BOTH 且两渠道皆空：回显 BOTH，无脱敏目标（实际登录时严格拦截）
        assertTrue(vo.getEnabled());
        assertEquals(TwoFactorChannelEnum.BOTH, vo.getChannel());
        assertNull(vo.getMaskedTarget());
    }

    @Test
    void forceConfigBoth_emailOnly_statusAndStartTwoFactor_symmetric() {
        // 对称性：同一仅绑邮箱用户，status 回显 channel=BOTH+脱敏邮箱，
        // startTwoFactor 返回 twoFactorChannel=BOTH 并签票成功（投递 EMAIL）
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);
        User user = new User();
        user.setId(9L);
        user.setEmail("user9@example.com");
        when(ssoUserService.getById(9L)).thenReturn(user);
        when(ticketService.issue(any())).thenReturn(TICKET);

        TwoFactorStatusVo statusVo = service.status(9L);
        assertTrue(statusVo.getEnabled());
        assertEquals(TwoFactorChannelEnum.BOTH, statusVo.getChannel());
        assertEquals("u****@example.com", statusVo.getMaskedTarget());

        LoginVo loginVo = service.startTwoFactor(user, AuthTypeEnum.EMAIL,
                "user9@example.com", null, null);
        assertEquals("BOTH", loginVo.getTwoFactorChannel());
        assertEquals("u****@example.com", loginVo.getTwoFactorMaskedTarget());
        ArgumentCaptor<TwoFactorTicketPayload> captor =
                ArgumentCaptor.forClass(TwoFactorTicketPayload.class);
        verify(ticketService).issue(captor.capture());
        assertEquals("BOTH", captor.getValue().getOpenedChannel());
        assertEquals("EMAIL", captor.getValue().getChannel());
        assertEquals("user9@example.com", captor.getValue().getTarget());
        // 两个入口均不读用户开通记录
        verify(mapper, never()).selectOneByQuery(any());
    }

    @Test
    void status_optionalEnabledUser_readsRecordAndReturnsChannel() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OPTIONAL);
        when(mapper.selectOneByQuery(any())).thenReturn(twoFactorRecord(1L, true, "PHONE"));
        when(ssoUserService.getById(1L)).thenReturn(fullUser());

        TwoFactorStatusVo vo = service.status(1L);

        assertEquals(TwoFactorStrategyEnum.OPTIONAL, vo.getStrategy());
        assertTrue(vo.getEnabled());
        assertEquals(TwoFactorChannelEnum.PHONE, vo.getChannel());
        assertEquals("138****1111", vo.getMaskedTarget());
    }

    @Test
    void status_optionalNotOpened_returnsDisabled() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OPTIONAL);
        when(mapper.selectOneByQuery(any())).thenReturn(null);

        TwoFactorStatusVo vo = service.status(1L);

        assertFalse(vo.getEnabled());
        assertNull(vo.getChannel());
        assertNull(vo.getMaskedTarget());
        verifyNoInteractions(ssoUserService);
    }

    // ==================== 开通/关闭/换渠道 ====================

    @Test
    void sendManageCode_phoneChannel_returnsMaskedPhoneOnly() {
        when(ssoUserService.getById(1L)).thenReturn(fullUser());
        when(otpService.send("manage", "13800001111")).thenReturn("138****1111");

        TwoFactorManageSendVo vo = service.sendManageCode(1L, TwoFactorChannelEnum.PHONE);

        assertEquals("138****1111", vo.getMaskedPhone());
        assertNull(vo.getMaskedEmail());
        // 单渠道只允许发一条
        verify(otpService, times(1)).send(eq("manage"), any());
    }

    @Test
    void sendManageCode_emailChannel_returnsMaskedEmailOnly() {
        when(ssoUserService.getById(1L)).thenReturn(fullUser());
        when(otpService.send("manage", "admin@example.com")).thenReturn("a***@example.com");

        TwoFactorManageSendVo vo = service.sendManageCode(1L, TwoFactorChannelEnum.EMAIL);

        assertNull(vo.getMaskedPhone());
        assertEquals("a***@example.com", vo.getMaskedEmail());
        verify(otpService, times(1)).send(eq("manage"), any());
    }

    @Test
    void sendManageCode_bothChannel_sendsToBothAndReturnsTwoMaskedTargets() {
        when(ssoUserService.getById(1L)).thenReturn(fullUser());
        when(otpService.send("manage", "13800001111")).thenReturn("138****1111");
        when(otpService.send("manage", "admin@example.com")).thenReturn("a***@example.com");

        TwoFactorManageSendVo vo = service.sendManageCode(1L, TwoFactorChannelEnum.BOTH);

        assertEquals("138****1111", vo.getMaskedPhone());
        assertEquals("a***@example.com", vo.getMaskedEmail());
        // 手机、邮箱各发一条，target 维度相互独立
        verify(otpService).send("manage", "13800001111");
        verify(otpService).send("manage", "admin@example.com");
    }

    @Test
    void sendManageCode_bothChannel_phoneMissing_throwsAndNoSend() {
        User emailOnly = new User();
        emailOnly.setId(1L);
        emailOnly.setEmail("admin@example.com");
        when(ssoUserService.getById(1L)).thenReturn(emailOnly);

        BizException ex = assertThrows(BizException.class,
                () -> service.sendManageCode(1L, TwoFactorChannelEnum.BOTH));
        assertTrue(ex.getMessage().contains("需同时绑定手机和邮箱"));
        verify(otpService, never()).send(any(), any());
    }

    @Test
    void sendManageCode_bothChannel_emailMissing_throwsAndNoSend() {
        User phoneOnly = new User();
        phoneOnly.setId(1L);
        phoneOnly.setPhone("13800001111");
        when(ssoUserService.getById(1L)).thenReturn(phoneOnly);

        BizException ex = assertThrows(BizException.class,
                () -> service.sendManageCode(1L, TwoFactorChannelEnum.BOTH));
        assertTrue(ex.getMessage().contains("需同时绑定手机和邮箱"));
        verify(otpService, never()).send(any(), any());
    }

    @Test
    void sendManageCode_targetMissing_throwsAndNoSend() {
        User user = new User();
        user.setId(1L);
        when(ssoUserService.getById(1L)).thenReturn(user);

        BizException ex = assertThrows(BizException.class,
                () -> service.sendManageCode(1L, TwoFactorChannelEnum.PHONE));
        assertTrue(ex.getMessage().contains("该渠道未绑定接收目标"));
        verify(otpService, never()).send(any(), any());
    }

    @Test
    void enable_newUser_verifiesCodeAndInserts() {
        when(ssoUserService.getById(1L)).thenReturn(fullUser());
        when(mapper.selectOneByQuery(any())).thenReturn(null);

        service.enable(1L, enableDto(TwoFactorChannelEnum.PHONE, "123456"));

        verify(otpService).verify("manage", "13800001111", "123456");
        ArgumentCaptor<UserTwoFactorAuth> captor =
                ArgumentCaptor.forClass(UserTwoFactorAuth.class);
        verify(mapper).insert(captor.capture());
        assertEquals(1L, captor.getValue().getUserId());
        assertTrue(captor.getValue().getEnabled());
        assertEquals("PHONE", captor.getValue().getChannel());
        verify(mapper, never()).update(any());
    }

    @Test
    void enable_existingDisabled_isIdempotentUpdate() {
        when(ssoUserService.getById(1L)).thenReturn(fullUser());
        when(mapper.selectOneByQuery(any())).thenReturn(twoFactorRecord(1L, false, "EMAIL"));

        // 第二次开通（或换渠道）不应产生新记录；BOTH 必须手机、邮箱双码均校验通过
        service.enable(1L, enableDto(TwoFactorChannelEnum.BOTH, "123456", "654321"));

        verify(otpService).verify("manage", "13800001111", "123456");
        verify(otpService).verify("manage", "admin@example.com", "654321");
        verify(mapper, never()).insert(any());
        ArgumentCaptor<UserTwoFactorAuth> captor =
                ArgumentCaptor.forClass(UserTwoFactorAuth.class);
        verify(mapper).update(captor.capture());
        assertTrue(captor.getValue().getEnabled());
        assertEquals("BOTH", captor.getValue().getChannel());
    }

    @Test
    void enable_bothChannel_bothCodesValid_verifiesBothAndInserts() {
        when(ssoUserService.getById(1L)).thenReturn(fullUser());
        when(mapper.selectOneByQuery(any())).thenReturn(null);

        service.enable(1L, enableDto(TwoFactorChannelEnum.BOTH, "111111", "222222"));

        // 顺序约束：先手机码后邮箱码，全部通过才写入
        InOrder inOrder = inOrder(otpService, mapper);
        inOrder.verify(otpService).verify("manage", "13800001111", "111111");
        inOrder.verify(otpService).verify("manage", "admin@example.com", "222222");
        inOrder.verify(mapper).insert(any(UserTwoFactorAuth.class));
        verify(mapper, never()).update(any());
    }

    @Test
    void enable_bothChannel_emailCodeMissing_rejectsBeforeAnyVerify() {
        when(ssoUserService.getById(1L)).thenReturn(fullUser());
        when(mapper.selectOneByQuery(any())).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> service.enable(1L, enableDto(TwoFactorChannelEnum.BOTH, "111111", null)));
        assertEquals("请输入邮箱验证码", ex.getMessage());
        // 邮箱码缺失在所有 verify 之前拦截：手机码也不得被消费
        verify(otpService, never()).verify(any(), any(), any());
        verify(mapper, never()).insert(any());
        verify(mapper, never()).update(any());
    }

    @Test
    void enable_bothChannel_emailCodeWrong_propagatesAndNoWrite() {
        when(ssoUserService.getById(1L)).thenReturn(fullUser());
        when(mapper.selectOneByQuery(any())).thenReturn(null);
        doThrow(new BizException("验证码错误，剩余 4 次机会"))
                .when(otpService).verify("manage", "admin@example.com", "000000");

        BizException ex = assertThrows(BizException.class,
                () -> service.enable(1L, enableDto(TwoFactorChannelEnum.BOTH, "111111", "000000")));
        assertEquals("验证码错误，剩余 4 次机会", ex.getMessage());
        // 手机码先通过并被消费，邮箱码失败原样抛出，不得写入开通记录
        verify(otpService).verify("manage", "13800001111", "111111");
        verify(otpService).verify("manage", "admin@example.com", "000000");
        verify(mapper, never()).insert(any());
        verify(mapper, never()).update(any());
    }

    @Test
    void enable_bothChannel_bindingIncomplete_rejectsWithoutAnyVerify() {
        User phoneOnly = new User();
        phoneOnly.setId(1L);
        phoneOnly.setPhone("13800001111");
        when(ssoUserService.getById(1L)).thenReturn(phoneOnly);

        BizException ex = assertThrows(BizException.class,
                () -> service.enable(1L, enableDto(TwoFactorChannelEnum.BOTH, "111111", "222222")));
        assertTrue(ex.getMessage().contains("需同时绑定手机和邮箱"));
        verify(otpService, never()).verify(any(), any(), any());
        verify(otpService, never()).send(any(), any());
        verify(mapper, never()).insert(any());
        verify(mapper, never()).update(any());
    }

    @Test
    void enable_wrongCode_throwsAndNoWrite() {
        when(ssoUserService.getById(1L)).thenReturn(fullUser());
        when(mapper.selectOneByQuery(any())).thenReturn(null);
        doThrow(new BizException("验证码错误，剩余 4 次机会"))
                .when(otpService).verify(any(), any(), any());

        assertThrows(BizException.class,
                () -> service.enable(1L, enableDto(TwoFactorChannelEnum.PHONE, "000000")));
        verify(mapper, never()).insert(any());
        verify(mapper, never()).update(any());
    }

    @Test
    void disable_underForce_rejectsWithoutCode() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.FORCE);

        BizException ex = assertThrows(BizException.class,
                () -> service.disable(1L, disableDto("123456")));
        assertTrue(ex.getMessage().contains("无法关闭"));
        verifyNoInteractions(otpService);
        verify(mapper, never()).update(any());
    }

    @Test
    void disable_notOpened_throws() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OPTIONAL);
        when(mapper.selectOneByQuery(any())).thenReturn(null);

        assertThrows(BizException.class,
                () -> service.disable(1L, disableDto("123456")));
        verify(otpService, never()).verify(any(), any(), any());
    }

    @Test
    void disable_success_verifiesCodeAndSetsDisabled() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OPTIONAL);
        when(mapper.selectOneByQuery(any())).thenReturn(twoFactorRecord(1L, true, "PHONE"));
        when(ssoUserService.getById(1L)).thenReturn(fullUser());

        service.disable(1L, disableDto("123456"));

        verify(otpService).verify("manage", "13800001111", "123456");
        // 单渠道记录即使带了邮箱码也不得校验邮箱
        verify(otpService, never()).verify(eq("manage"), eq("admin@example.com"), any());
        ArgumentCaptor<UserTwoFactorAuth> captor =
                ArgumentCaptor.forClass(UserTwoFactorAuth.class);
        verify(mapper).update(captor.capture());
        assertFalse(captor.getValue().getEnabled());
    }

    @Test
    void disable_bothChannel_bothCodesValid_verifiesBothAndSetsDisabled() {
        // BOTH 记录关闭与开通同口径：手机码、邮箱码双码均通过才允许关闭
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OPTIONAL);
        when(mapper.selectOneByQuery(any())).thenReturn(twoFactorRecord(1L, true, "BOTH"));
        when(ssoUserService.getById(1L)).thenReturn(fullUser());

        service.disable(1L, disableDto("111111", "222222"));

        // 顺序约束：先手机码后邮箱码，全部通过才更新为关闭
        InOrder inOrder = inOrder(otpService, mapper);
        inOrder.verify(otpService).verify("manage", "13800001111", "111111");
        inOrder.verify(otpService).verify("manage", "admin@example.com", "222222");
        inOrder.verify(mapper).update(any(UserTwoFactorAuth.class));
        ArgumentCaptor<UserTwoFactorAuth> captor =
                ArgumentCaptor.forClass(UserTwoFactorAuth.class);
        verify(mapper).update(captor.capture());
        assertFalse(captor.getValue().getEnabled());
    }

    @Test
    void disable_bothChannel_emailCodeMissing_rejectsBeforeAnyVerify() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OPTIONAL);
        when(mapper.selectOneByQuery(any())).thenReturn(twoFactorRecord(1L, true, "BOTH"));
        when(ssoUserService.getById(1L)).thenReturn(fullUser());

        BizException ex = assertThrows(BizException.class,
                () -> service.disable(1L, disableDto("111111", null)));
        assertEquals("请输入邮箱验证码", ex.getMessage());
        // 邮箱码缺失在所有 verify 之前拦截：手机码也不得被消费
        verify(otpService, never()).verify(any(), any(), any());
        verify(mapper, never()).update(any());
    }

    @Test
    void disable_bothChannel_emailCodeWrong_propagatesAndNoUpdate() {
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OPTIONAL);
        when(mapper.selectOneByQuery(any())).thenReturn(twoFactorRecord(1L, true, "BOTH"));
        when(ssoUserService.getById(1L)).thenReturn(fullUser());
        doThrow(new BizException("验证码错误，剩余 4 次机会"))
                .when(otpService).verify("manage", "admin@example.com", "000000");

        BizException ex = assertThrows(BizException.class,
                () -> service.disable(1L, disableDto("111111", "000000")));
        assertEquals("验证码错误，剩余 4 次机会", ex.getMessage());
        // 手机码先通过并被消费，邮箱码失败原样抛出，不得关闭
        verify(otpService).verify("manage", "13800001111", "111111");
        verify(otpService).verify("manage", "admin@example.com", "000000");
        verify(mapper, never()).update(any());
    }

    @Test
    void disable_bothChannel_bindingIncomplete_rejectsWithoutAnyVerify() {
        // BOTH 记录关闭时用户已解绑邮箱：双绑定前置校验拦截，不消费任何验证码
        when(strategyResolver.resolve()).thenReturn(TwoFactorStrategyEnum.OPTIONAL);
        when(mapper.selectOneByQuery(any())).thenReturn(twoFactorRecord(1L, true, "BOTH"));
        User phoneOnly = new User();
        phoneOnly.setId(1L);
        phoneOnly.setPhone("13800001111");
        when(ssoUserService.getById(1L)).thenReturn(phoneOnly);

        BizException ex = assertThrows(BizException.class,
                () -> service.disable(1L, disableDto("111111", "222222")));
        assertTrue(ex.getMessage().contains("需同时绑定手机和邮箱"));
        verify(otpService, never()).verify(any(), any(), any());
        verify(mapper, never()).update(any());
    }

    @Test
    void changeChannel_sameFlowAsEnable_insertsWithNewChannel() {
        User emailOnly = new User();
        emailOnly.setId(2L);
        emailOnly.setEmail("user2@example.com");
        when(ssoUserService.getById(2L)).thenReturn(emailOnly);
        when(mapper.selectOneByQuery(any())).thenReturn(null);

        service.changeChannel(2L, enableDto(TwoFactorChannelEnum.EMAIL, "654321"));

        verify(otpService).verify("manage", "user2@example.com", "654321");
        ArgumentCaptor<UserTwoFactorAuth> captor =
                ArgumentCaptor.forClass(UserTwoFactorAuth.class);
        verify(mapper).insert(captor.capture());
        assertEquals("EMAIL", captor.getValue().getChannel());
    }

    // ==================== 夹具 ====================

    private User fullUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPhone("13800001111");
        user.setEmail("admin@example.com");
        return user;
    }

    private UserTwoFactorAuth twoFactorRecord(Long userId, boolean enabled, String channel) {
        UserTwoFactorAuth record = new UserTwoFactorAuth();
        record.setId(userId + 1000L);
        record.setUserId(userId);
        record.setEnabled(enabled);
        record.setChannel(channel);
        return record;
    }

    private TwoFactorTicketPayload loginPayload(String openedChannel, String phone, String email) {
        TwoFactorTicketPayload payload = new TwoFactorTicketPayload();
        payload.setUserId(1L);
        payload.setOpenedChannel(openedChannel);
        // 初始投递渠道：BOTH 默认手机，单渠道与开通渠道一致
        payload.setChannel("BOTH".equals(openedChannel) ? "PHONE" : openedChannel);
        payload.setTarget(phone != null ? phone : email);
        payload.setPhone(phone);
        payload.setEmail(email);
        return payload;
    }

    private TwoFactorSendDto sendDto(String channel) {
        TwoFactorSendDto dto = new TwoFactorSendDto();
        dto.setTicket(TICKET);
        dto.setChannel(channel);
        return dto;
    }

    /** 模拟 Redis 序列化往返：复制换渠道流程涉及的字段，得到相互独立的载荷快照 */
    private TwoFactorTicketPayload copyPayload(TwoFactorTicketPayload source) {
        TwoFactorTicketPayload copy = new TwoFactorTicketPayload();
        copy.setUserId(source.getUserId());
        copy.setOpenedChannel(source.getOpenedChannel());
        copy.setChannel(source.getChannel());
        copy.setTarget(source.getTarget());
        copy.setPhone(source.getPhone());
        copy.setEmail(source.getEmail());
        return copy;
    }

    private TwoFactorVerifyDto verifyDto(String code) {
        TwoFactorVerifyDto dto = new TwoFactorVerifyDto();
        dto.setTicket(TICKET);
        dto.setCode(code);
        return dto;
    }

    private TwoFactorEnableDto enableDto(TwoFactorChannelEnum channel, String code) {
        TwoFactorEnableDto dto = new TwoFactorEnableDto();
        dto.setChannel(channel);
        dto.setCode(code);
        return dto;
    }

    private TwoFactorEnableDto enableDto(TwoFactorChannelEnum channel, String code,
                                         String emailCode) {
        TwoFactorEnableDto dto = enableDto(channel, code);
        dto.setEmailCode(emailCode);
        return dto;
    }

    private TwoFactorDisableDto disableDto(String code) {
        TwoFactorDisableDto dto = new TwoFactorDisableDto();
        dto.setCode(code);
        return dto;
    }

    private TwoFactorDisableDto disableDto(String code, String emailCode) {
        TwoFactorDisableDto dto = disableDto(code);
        dto.setEmailCode(emailCode);
        return dto;
    }
}
