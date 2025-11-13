package top.mddata.workbench.service;


import top.mddata.base.base.R;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.RegisterByEmailDto;
import top.mddata.workbench.dto.RegisterByPhoneDto;
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
     * 发送手机 短信
     * @param phone 手机号
     * @param templateCode 短信模板
     * @return 是否成功
     */
    R<String> sendPhoneCode(String phone, String templateCode);

    /**
     * 发送邮箱验证码
     * @param email 邮箱
     * @param templateCode 邮箱编码
     * @return 是否成功
     */
    R<String> sendEmailCode(String email, String templateCode);

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
}
