package top.mddata.workbench.service;

import top.mddata.workbench.vo.CaptchaVo;

/**
 * 验证码 业务接口
 * @author henhen
 * @since 2025/12/27 19:53
 */
public interface VerificationCodeService {

    /**
     * 创建图片验证码
     */
    CaptchaVo createImg();


    /**
     * 发送手机 验证码
     * @param phone 手机号
     * @param templateCode 短信模板
     * @return 验证码Key
     */
    String sendPhoneCode(String phone, String templateCode);

    /**
     * 发送邮箱 验证码
     * @param email 邮箱
     * @param templateCode 邮箱编码
     * @return 验证码Key
     */
    String sendEmailCode(String email, String templateCode);

}
