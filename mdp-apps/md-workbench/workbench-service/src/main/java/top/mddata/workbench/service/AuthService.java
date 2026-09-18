package top.mddata.workbench.service;


import top.mddata.base.base.R;
import top.mddata.workbench.dto.ForgetPasswordByEmailDto;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.RegisterByEmailDto;
import top.mddata.workbench.dto.RegisterByPhoneDto;
import top.mddata.workbench.dto.RegisterByUsernameDto;
import top.mddata.workbench.vo.LoginVo;

/**
 * 认证
 *
 * @author henhen6
 * @since 2025/6/30 16:38
 */
public interface AuthService {
    /**
     * 根据登录类型，适配不同的登录方式
     * @param login 登录参数
     * @return 用户信息
     */
    R<LoginVo> login(LoginDto login);

    /**
     * SSO 客户端 ticket 模式登录：ticket 已由客户端校验通过，此处完成本地登录，
     * 并将与普通登录一致的用户常用信息写入 TokenSession。
     * 登录日志事件统一在 SsoServerController#getRedirectUrl 签发 ticket 时推送，此处不记录
     *
     * @param loginId            SSO 校验返回的登录标识（用户id）
     * @param remainTokenTimeout ticket 对应的 token 剩余有效期
     * @param deviceId           设备id
     * @return 本次登录生成的 token
     */
    String loginByTicket(Object loginId, Long remainTokenTimeout, String deviceId);

    /**
     * 根据邮箱注册
     * @param register 参数
     * @return 邮箱
     */
    String registerByEmail(RegisterByEmailDto register);

    /**
     * 根据手机号注册
     * @param register 参数
     * @return 手机号
     */
    String registerByPhone(RegisterByPhoneDto register);

    /**
     * 根据用户名注册账号
     * @param register 参数
     * @return 用户名
     */
    String registerByUsername(RegisterByUsernameDto register);

    /**
     * 忘记密码
     * @param email 邮箱
     * @return true 发送成功
     */
    Boolean forgetPassword(String email);

    /**
     * 验证找回密码的 邮箱token是否过期
     * @param token token
     * @return true 未过期，false 已过期
     */
    Boolean checkToken(String token);


    /**
     * 根据token修改邮箱
     * @param dto 参数
     * @return 是否成功
     */
    Boolean updateEmailByToken(ForgetPasswordByEmailDto dto);
}
