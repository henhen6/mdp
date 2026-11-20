package top.mddata.workbench.service.twofactor;

import top.mddata.common.entity.User;
import top.mddata.workbench.dto.TwoFactorDisableDto;
import top.mddata.workbench.dto.TwoFactorEnableDto;
import top.mddata.workbench.dto.TwoFactorSendDto;
import top.mddata.workbench.dto.TwoFactorVerifyDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.enumeration.TwoFactorChannelEnum;
import top.mddata.workbench.service.twofactor.TwoFactorTicketService.TwoFactorTicketPayload;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.TwoFactorManageSendVo;
import top.mddata.workbench.vo.TwoFactorStatusVo;

/**
 * 双因素认证核心服务。
 *
 * @author tangyh
 * @since 2026-11-16
 */
public interface TwoFactorAuthService {

    /**
     * 触发判定（spec 3.2 矩阵）：OFF 不触发；OPTIONAL 仅已开通用户触发；
     * FORCE 全员触发（无可用渠道用户由登录编排拦截）。
     *
     * @param user 密码校验通过的登录用户
     * @return true 表示需要二次验证
     */
    boolean shouldTrigger(User user);

    /**
     * 开始二次验证：签发票据（不创建会话），返回 TWO_FACTOR 状态的 LoginVo。
     *
     * @param user       密码校验通过的用户
     * @param authType   认证方式（用于登录日志）
     * @param account    登录账号（用于登录日志）
     * @param deviceId   设备标识
     * @param deviceInfo 设备信息
     * @return 含票据与脱敏目标的登录返回值
     */
    LoginVo startTwoFactor(User user, AuthTypeEnum authType, String account,
                           String deviceId, String deviceInfo);

    /**
     * 凭票据发送登录二次验证 OTP，返回脱敏目标。
     * BOTH 渠道不传 channel 时手机优先，显式传 EMAIL 时切换邮箱。
     *
     * @param dto 票据与可选渠道
     * @return 脱敏接收目标
     */
    String sendLoginOtp(TwoFactorSendDto dto);

    /**
     * 校验登录二次验证 OTP。OTP 校验通过后才消费票据，失败累计票据失败次数。
     *
     * @param dto 票据与验证码
     * @return 票据载荷（含登录上下文），供控制器签发会话
     */
    TwoFactorTicketPayload verifyLogin(TwoFactorVerifyDto dto);

    /**
     * 查询用户双因素状态（安全设置页）。
     *
     * @param userId 用户 id
     * @return 开通状态、渠道、全局策略、脱敏目标
     */
    TwoFactorStatusVo status(Long userId);

    /**
     * 开通/换渠道前发送验证码（scene=manage）。
     * PHONE/EMAIL 仅向所选单渠道发送；BOTH（手机或邮箱均可）要求手机与邮箱均已绑定，
     * 两个渠道各发送一条验证码（频控与失败计数按 target 各自独立）。
     *
     * @param userId  用户 id
     * @param channel 目标渠道
     * @return 各渠道脱敏接收目标，未发送的渠道字段为 null
     */
    TwoFactorManageSendVo sendManageCode(Long userId, TwoFactorChannelEnum channel);

    /**
     * 开通双因素认证：校验 manage 场景验证码后幂等写入开通记录。
     * channel=BOTH 时必须依次校验手机验证码（code）与邮箱验证码（emailCode），
     * 任一失败原样抛出且不写入；PHONE/EMAIL 仅校验所选单渠道。
     *
     * @param userId 用户 id
     * @param dto    渠道与验证码
     */
    void enable(Long userId, TwoFactorEnableDto dto);

    /**
     * 关闭双因素认证：FORCE 策略下直接拒绝；BOTH 记录需依次校验手机码（code）与
     * 邮箱码（emailCode），PHONE/EMAIL 记录仅校验当前单渠道码。
     *
     * @param userId 用户 id
     * @param dto    验证码（BOTH 时含手机码与邮箱码）
     */
    void disable(Long userId, TwoFactorDisableDto dto);

    /**
     * 更换验证渠道（语义同开通，独立方法便于日志与鉴权区分）。
     *
     * @param userId 用户 id
     * @param dto    新渠道与新渠道收到的验证码
     */
    void changeChannel(Long userId, TwoFactorEnableDto dto);
}
