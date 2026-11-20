package top.mddata.workbench.service.impl;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.exception.BizException;
import top.mddata.base.utils.ArgumentAssert;
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
import top.mddata.workbench.service.TwoFactorAuthService;
import top.mddata.workbench.service.TwoFactorOtpService;
import top.mddata.workbench.service.TwoFactorStrategyResolver;
import top.mddata.workbench.service.TwoFactorTicketService;
import top.mddata.workbench.service.TwoFactorTicketService.TwoFactorTicketPayload;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.TwoFactorManageSendVo;
import top.mddata.workbench.vo.TwoFactorStatusVo;

import static top.mddata.common.cache.workbench.TwoFactorCacheKeyBuilder.SCENE_LOGIN;
import static top.mddata.common.cache.workbench.TwoFactorCacheKeyBuilder.SCENE_MANAGE;

/**
 * 双因素认证核心服务实现。
 *
 * <p>登录票据校验为两段式：先 peek 校验票据，再校验 OTP，全部成功后才 consume 删除票据；
 * OTP 失败先累计票据失败次数再原样抛出，保证 5 次失败作废机制有效。</p>
 *
 * @author tangyh
 * @since 2026-11-16
 */
@Service
@RequiredArgsConstructor
public class TwoFactorAuthServiceImpl implements TwoFactorAuthService {

    /** LoginVo 中待二次验证的状态标识 */
    private static final String STATUS_TWO_FACTOR = "TWO_FACTOR";

    private static final String MOBILE_PATTERN = "^1\\d{10}$";

    private final UserTwoFactorAuthMapper twoFactorAuthMapper;
    private final TwoFactorStrategyResolver strategyResolver;
    private final TwoFactorTicketService ticketService;
    private final TwoFactorOtpService otpService;
    private final SsoUserService ssoUserService;

    @Override
    public boolean shouldTrigger(User user) {
        TwoFactorStrategyEnum strategy = strategyResolver.resolve();
        if (strategy == TwoFactorStrategyEnum.OFF) {
            return false;
        }
        if (strategy == TwoFactorStrategyEnum.FORCE) {
            // 无手机/邮箱的用户也返回 true，由登录编排统一拦截提示联系管理员
            return true;
        }
        UserTwoFactorAuth record = findByUserId(user.getId());
        return record != null && Boolean.TRUE.equals(record.getEnabled());
    }

    @Override
    public LoginVo startTwoFactor(User user, AuthTypeEnum authType, String account,
                                  String deviceId, String deviceInfo) {
        // opened 为用户开通渠道：FORCE 取全局强制渠道配置（忽略用户记录与绑定情况）；
        // OPTIONAL 取用户开通记录的自选渠道。
        // BOTH 实际投递渠道按绑定自适应（手机优先，无手机取邮箱），与 status 脱敏目标、
        // sendLoginOtp“不传渠道”同口径：仅邮箱用户投递邮箱，双空仍在下方拦截
        TwoFactorStrategyEnum strategy = strategyResolver.resolve();
        TwoFactorChannelEnum opened = strategy == TwoFactorStrategyEnum.FORCE
                ? strategyResolver.resolveForceChannel()
                : resolveStartChannel(user);
        TwoFactorChannelEnum channel = opened == TwoFactorChannelEnum.BOTH
                ? (StrUtil.isNotEmpty(user.getPhone())
                ? TwoFactorChannelEnum.PHONE : TwoFactorChannelEnum.EMAIL)
                : opened;
        // PHONE/EMAIL 档缺指定渠道严格拦截不降级；BOTH 档双空拦截，文案按配置档位具体化
        String target = requireTarget(user.getPhone(), user.getEmail(), channel,
                forceAbsentMessage(opened));

        TwoFactorTicketPayload payload = new TwoFactorTicketPayload();
        payload.setUserId(user.getId());
        payload.setAuthType(authType.name());
        payload.setAccount(account);
        payload.setDeviceId(deviceId);
        payload.setDeviceInfo(deviceInfo);
        // 开通渠道固定不变，投递渠道随切换回写，二者不可混用
        payload.setOpenedChannel(opened.name());
        payload.setChannel(channel.name());
        payload.setTarget(target);
        // 冻结手机/邮箱快照：send 默认重发/同渠道重发直接沿用，显式换渠道时实时重查
        payload.setPhone(user.getPhone());
        payload.setEmail(user.getEmail());
        String ticket = ticketService.issue(payload);

        LoginVo vo = LoginVo.token();
        vo.setStatus(STATUS_TWO_FACTOR);
        vo.setTwoFactorTicket(ticket);
        // 回传开通渠道而非投递渠道：BOTH 时前端据此展示“换一种方式”入口
        vo.setTwoFactorChannel(opened.name());
        vo.setTwoFactorMaskedTarget(maskTarget(target));
        return vo;
    }

    @Override
    public String sendLoginOtp(TwoFactorSendDto dto) {
        TwoFactorTicketPayload payload = ticketService.peek(dto.getTicket());
        ArgumentAssert.notNull(payload, () -> new BizException("票据无效或已过期，请重新登录"));

        // 切换权限必须以签发后不变的 openedChannel 判定；channel 是投递渠道，会被本方法回写
        TwoFactorChannelEnum channel = resolveSendChannel(payload, dto.getChannel());
        // 仅显式换渠道才实时重查；不传渠道的默认发送与同渠道重发沿用签发快照
        boolean switchChannel = StrUtil.isNotEmpty(dto.getChannel())
                                && !channel.name().equals(payload.getChannel());
        String target;
        if (switchChannel) {
            // 显式换渠道实时重查绑定：载荷 phone/email 是签发时快照（TTL 300s），窗口期内
            // 用户可能换绑/解绑，沿用快照会把 OTP 发往旧目标；仅换渠道时支付一次查库
            User fresh = requireUser(payload.getUserId());
            target = requireTarget(fresh.getPhone(), fresh.getEmail(), channel, "该渠道未绑定接收目标");
            payload.setPhone(fresh.getPhone());
            payload.setEmail(fresh.getEmail());
        } else {
            // 默认重发/同渠道重发沿用快照，减少登录热路径查库
            target = requireTarget(payload.getPhone(), payload.getEmail(), channel,
                    "该渠道未绑定接收目标");
        }

        // 仅回写实际投递渠道与目标（verify 按 target 校验），openedChannel 保持不变
        payload.setChannel(channel.name());
        payload.setTarget(target);
        ticketService.updatePayload(dto.getTicket(), payload);
        return otpService.send(SCENE_LOGIN, target);
    }

    @Override
    public TwoFactorTicketPayload verifyLogin(TwoFactorVerifyDto dto) {
        // 第一段：只读校验票据（peek 内含失败超限拦截），不消费
        TwoFactorTicketPayload payload = ticketService.peek(dto.getTicket());
        ArgumentAssert.notNull(payload, () -> new BizException("票据无效或已过期，请重新登录"));
        try {
            // 第二段：OTP 失败先累计票据失败次数（达 5 次会删票并抛出），再原样抛出
            otpService.verify(SCENE_LOGIN, payload.getTarget(), dto.getCode());
        } catch (BizException e) {
            ticketService.recordFail(dto.getTicket());
            throw e;
        }
        // 全部成功才消费票据，防止验证码错误也被一次性放行
        TwoFactorTicketPayload consumed = ticketService.consume(dto.getTicket());
        ArgumentAssert.notNull(consumed, () -> new BizException("票据无效或已过期，请重新登录"));
        return consumed;
    }

    @Override
    public TwoFactorStatusVo status(Long userId) {
        TwoFactorStatusVo vo = new TwoFactorStatusVo();
        TwoFactorStrategyEnum strategy = strategyResolver.resolve();
        vo.setStrategy(strategy);
        if (strategy == TwoFactorStrategyEnum.OFF) {
            // 功能关闭：忽略用户已有配置，前端按策略展示
            vo.setEnabled(false);
            return vo;
        }
        if (strategy == TwoFactorStrategyEnum.FORCE) {
            // 强制全员：忽略用户配置，渠道回显全局强制渠道配置（不按绑定自适应）
            User user = requireUser(userId);
            TwoFactorChannelEnum effective = strategyResolver.resolveForceChannel();
            vo.setEnabled(true);
            vo.setChannel(effective);
            // 缺指定渠道时脱敏目标为 null，前端已有兜底；复用已加载用户避免重复查库
            vo.setMaskedTarget(resolveMaskedTarget(user, effective));
            return vo;
        }
        // OPTIONAL：读用户配置
        UserTwoFactorAuth record = findByUserId(userId);
        boolean enabled = record != null && Boolean.TRUE.equals(record.getEnabled());
        vo.setEnabled(enabled);
        if (enabled) {
            TwoFactorChannelEnum channel = record.channelEnum();
            vo.setChannel(channel);
            vo.setMaskedTarget(resolveMaskedTarget(userId, channel));
        }
        return vo;
    }

    @Override
    public TwoFactorManageSendVo sendManageCode(Long userId, TwoFactorChannelEnum channel) {
        ArgumentAssert.notNull(channel, () -> new BizException("验证渠道不能为空"));
        User user = requireUser(userId);
        TwoFactorManageSendVo vo = new TwoFactorManageSendVo();
        if (channel == TwoFactorChannelEnum.BOTH) {
            // BOTH 开通后登录可任选手机或邮箱二次验证，故发码阶段必须两渠道都可达
            requireBothBound(user);
            // 两个 target 各自独立 60s 频控与失败计数，互不影响
            vo.setMaskedPhone(otpService.send(SCENE_MANAGE, user.getPhone()));
            vo.setMaskedEmail(otpService.send(SCENE_MANAGE, user.getEmail()));
            return vo;
        }
        String target = requireTarget(user.getPhone(), user.getEmail(), channel,
                "该渠道未绑定接收目标");
        String masked = otpService.send(SCENE_MANAGE, target);
        if (channel == TwoFactorChannelEnum.PHONE) {
            vo.setMaskedPhone(masked);
        } else {
            vo.setMaskedEmail(masked);
        }
        return vo;
    }

    @Override
    public void enable(Long userId, TwoFactorEnableDto dto) {
        User user = requireUser(userId);
        if (dto.getChannel() == TwoFactorChannelEnum.BOTH) {
            requireBothBound(user);
            // 手机码 code 已有 @NotBlank；邮箱码单渠道场景不使用，不加 Bean Validation，
            // 在此 fail fast 拦截，避免空码进入 verify 触发不相关的计数逻辑
            ArgumentAssert.notEmpty(dto.getEmailCode(),
                    () -> new BizException("请输入邮箱验证码"));
            // 先验手机码再验邮箱码：任一失败原样抛出（otpService 内含按 target 的失败计数），
            // 成功的验证码会被立即消费，用户需重新获取两渠道验证码
            otpService.verify(SCENE_MANAGE, user.getPhone(), dto.getCode());
            otpService.verify(SCENE_MANAGE, user.getEmail(), dto.getEmailCode());
        } else {
            String target = requireTarget(user.getPhone(), user.getEmail(), dto.getChannel(),
                    "该渠道未绑定接收目标");
            otpService.verify(SCENE_MANAGE, target, dto.getCode());
        }
        upsertEnabledRecord(userId, dto.getChannel());
    }

    @Override
    public void disable(Long userId, TwoFactorDisableDto dto) {
        ArgumentAssert.isFalse(strategyResolver.resolve() == TwoFactorStrategyEnum.FORCE,
                () -> new BizException("管理员已强制启用双因素认证，无法关闭"));
        UserTwoFactorAuth record = findByUserId(userId);
        ArgumentAssert.notNull(record, () -> new BizException("尚未开通双因素认证，无需关闭"));
        ArgumentAssert.isTrue(Boolean.TRUE.equals(record.getEnabled()),
                () -> new BizException("双因素认证已处于关闭状态，无需重复操作"));

        User user = requireUser(userId);
        if (record.channelEnum() == TwoFactorChannelEnum.BOTH) {
            // BOTH 记录的关闭与开通/换渠道同口径双码校验，与 sendManageCode 的 BOTH 双发对称：
            // 否则邮箱码空发，用户填入邮箱码又会被当手机码拒绝
            requireBothBound(user);
            // 手机码 code 已有 @NotBlank；邮箱码单渠道场景不使用，与开通一致在此 fail fast
            ArgumentAssert.notEmpty(dto.getEmailCode(),
                    () -> new BizException("请输入邮箱验证码"));
            otpService.verify(SCENE_MANAGE, user.getPhone(), dto.getCode());
            otpService.verify(SCENE_MANAGE, user.getEmail(), dto.getEmailCode());
        } else {
            String target = requireTarget(user.getPhone(), user.getEmail(), record.channelEnum(),
                    "双因素认证渠道未绑定手机或邮箱，请联系管理员");
            otpService.verify(SCENE_MANAGE, target, dto.getCode());
        }
        record.setEnabled(false);
        twoFactorAuthMapper.update(record);
    }

    @Override
    public void changeChannel(Long userId, TwoFactorEnableDto dto) {
        // 换渠道复用开通逻辑：向新渠道发码、校验后覆盖 channel
        enable(userId, dto);
    }

    // ==================== 私有方法 ====================

    private UserTwoFactorAuth findByUserId(Long userId) {
        return twoFactorAuthMapper.selectOneByQuery(
                QueryWrapper.create().eq(UserTwoFactorAuth::getUserId, userId));
    }

    private User requireUser(Long userId) {
        User user = ssoUserService.getById(userId);
        ArgumentAssert.notNull(user, () -> new BizException("用户不存在，userId=" + userId));
        return user;
    }

    /**
     * OPTIONAL 下解析开通渠道：shouldTrigger 已保证存在 enabled 记录，返回用户自选渠道。
     * 无记录兜底理论不可达（防御性按绑定返回单渠道，两者皆空由目标校验拦截）；
     * FORCE 不经此方法，开通渠道统一取 {@link TwoFactorStrategyResolver#resolveForceChannel()}。
     */
    private TwoFactorChannelEnum resolveStartChannel(User user) {
        UserTwoFactorAuth record = findByUserId(user.getId());
        if (record != null && Boolean.TRUE.equals(record.getEnabled())) {
            return record.channelEnum();
        }
        return StrUtil.isNotEmpty(user.getPhone()) ? TwoFactorChannelEnum.PHONE : TwoFactorChannelEnum.EMAIL;
    }

    /**
     * 解析登录时的投递渠道：不传渠道时单渠道沿用开通渠道，BOTH 按票据绑定快照自适应
     * （手机优先，无手机取邮箱），与签票口径一致，保证仅绑邮箱用户首条验证码可达邮箱；
     * 显式切换仅在 opened=BOTH 时允许——FORCE 两渠道齐用户 opened 已解析为 BOTH，
     * 单渠道用户无另一绑定可切；OPTIONAL 单渠道用户仅能沿用原渠道，用户自选不被覆盖。
     */
    private TwoFactorChannelEnum resolveSendChannel(TwoFactorTicketPayload payload,
                                                    String requestChannel) {
        TwoFactorChannelEnum opened = TwoFactorChannelEnum.fromCode(payload.getOpenedChannel());
        if (StrUtil.isEmpty(requestChannel)) {
            // BOTH 不传渠道时手机优先、无手机取邮箱（快照在签票/显式换渠道时已冻结）
            return opened == TwoFactorChannelEnum.BOTH
                    ? (StrUtil.isNotEmpty(payload.getPhone())
                    ? TwoFactorChannelEnum.PHONE : TwoFactorChannelEnum.EMAIL)
                    : opened;
        }
        TwoFactorChannelEnum requested = TwoFactorChannelEnum.fromCode(requestChannel);
        ArgumentAssert.isTrue(opened == TwoFactorChannelEnum.BOTH || opened == requested,
                () -> new BizException("当前开通渠道不支持切换验证方式"));
        return requested;
    }

    /**
     * FORCE 下缺少配置指定渠道时的拦截文案：单渠道档明确引导绑定指定渠道并联系管理员，
     * BOTH 档保持通用文案。
     */
    private String forceAbsentMessage(TwoFactorChannelEnum channel) {
        return switch (channel) {
            case PHONE -> "系统指定验证渠道为手机，请绑定手机号后登录，或联系管理员";
            case EMAIL -> "系统指定验证渠道为邮箱，请绑定邮箱后登录，或联系管理员";
            case BOTH -> "双因素认证渠道未绑定手机或邮箱，请联系管理员";
        };
    }

    private String resolveMaskedTarget(Long userId, TwoFactorChannelEnum channel) {
        return resolveMaskedTarget(requireUser(userId), channel);
    }

    private String resolveMaskedTarget(User user, TwoFactorChannelEnum channel) {
        // BOTH 手机优先无手机取邮箱，目标缺失返回 null
        String target = resolveTarget(user.getPhone(), user.getEmail(), channel);
        return StrUtil.isNotEmpty(target) ? maskTarget(target) : null;
    }

    private String requireTarget(String phone, String email, TwoFactorChannelEnum channel,
                                 String absentMessage) {
        String target = resolveTarget(phone, email, channel);
        ArgumentAssert.notEmpty(target, () -> new BizException(absentMessage));
        return target;
    }

    /**
     * BOTH（手机或邮箱均可）管理操作前置校验：发码与开通都要求手机、邮箱同时已绑定，
     * 缺少任一则拒绝，防止开通后另一渠道实际不可达。
     */
    private void requireBothBound(User user) {
        ArgumentAssert.isTrue(StrUtil.isNotEmpty(user.getPhone())
                              && StrUtil.isNotEmpty(user.getEmail()),
                () -> new BizException("开通『手机或邮箱均可』需同时绑定手机和邮箱"));
    }

    /**
     * 按渠道解析验证码接收目标：PHONE 取手机，EMAIL 取邮箱，BOTH 手机优先无手机取邮箱；
     * 目标缺失返回 null，由调用方按场景给出具体错误。
     */
    private String resolveTarget(String phone, String email, TwoFactorChannelEnum channel) {
        return switch (channel) {
            case PHONE -> StrUtil.isNotEmpty(phone) ? phone : null;
            case EMAIL -> StrUtil.isNotEmpty(email) ? email : null;
            case BOTH -> StrUtil.isNotEmpty(phone) ? phone : email;
        };
    }

    private String maskTarget(String target) {
        return target.matches(MOBILE_PATTERN)
                ? DesensitizedUtil.mobilePhone(target) : DesensitizedUtil.email(target);
    }

    private void upsertEnabledRecord(Long userId, TwoFactorChannelEnum channel) {
        UserTwoFactorAuth record = findByUserId(userId);
        if (record == null) {
            // Base 类 @Builder 在继承层级存在字段丢失陷阱，统一 new + setter
            record = newRecord(userId, channel);
            twoFactorAuthMapper.insert(record);
            return;
        }
        record.setEnabled(true).setChannel(channel.name());
        twoFactorAuthMapper.update(record);
    }

    private UserTwoFactorAuth newRecord(Long userId, TwoFactorChannelEnum channel) {
        UserTwoFactorAuth record = new UserTwoFactorAuth();
        record.setUserId(userId);
        record.setEnabled(true);
        record.setChannel(channel.name());
        return record;
    }
}
