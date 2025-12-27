package top.mddata.workbench.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.wf.captcha.base.Captcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.captcha.graphic.service.GraphicCaptchaService;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.workbench.CaptchaCacheKeyBuilder;
import top.mddata.common.constant.ConfigKey;
import top.mddata.common.constant.MsgTemplateKey;
import top.mddata.console.message.dto.MsgSendDto;
import top.mddata.console.message.dto.MsgSendMailDto;
import top.mddata.console.message.dto.MsgSendSmsDto;
import top.mddata.console.message.facade.MsgFacade;
import top.mddata.console.system.facade.ConfigFacade;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.service.VerificationCodeService;
import top.mddata.workbench.vo.CaptchaVo;

import static top.mddata.workbench.handler.impl.CaptchaLoginStrategyImpl.GRANT_TYPE;

/**
 * 验证码服务实现类
 * @author henhen
 * @since 2025/12/27 19:54
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {
    private final GraphicCaptchaService graphicCaptchaService;
    private final ConfigFacade configFacade;
    private final MsgFacade msgFacade;
    private final CacheOps cacheOps;
    private final SsoUserService ssoUserService;

    @Override
    public R<String> sendPhoneCode(String phone, String templateCode) {
        if (MsgTemplateKey.Sms.PHONE_REGISTER.equals(templateCode)) {
            // 查user表判断重复
            boolean flag = ssoUserService.checkPhone(phone, null);
            ArgumentAssert.isFalse(flag, "该手机号已经被注册");
        } else if (MsgTemplateKey.Sms.PHONE_LOGIN.equals(templateCode)) {
            //查user表判断是否存在
            boolean flag = ssoUserService.checkPhone(phone, null);
            ArgumentAssert.isTrue(flag, "该手机号尚未注册，请先注册后在登陆。");
        } else if (MsgTemplateKey.Sms.PHONE_EDIT.equals(templateCode)) {
            //查user表判断是否存在
            boolean flag = ssoUserService.checkPhone(phone, null);
            ArgumentAssert.isFalse(flag, "该手机号已经被他人使用");
        }

        String code = RandomUtil.randomNumbers(6);
        String key = RandomUtil.randomNumbers(10);
        CacheKey cacheKey = CaptchaCacheKeyBuilder.build(key, templateCode);
        cacheOps.set(cacheKey, code);

        log.info("短信验证码 cacheKey={}, code={}", cacheKey, code);

        MsgSendDto msgSendDto = MsgSendSmsDto.buildApiSender()
                .addRecipient(phone)
                .setTemplateKey(templateCode)
                // code 是在短信模版中配置的 占位符参数
                .addParam("code", code);

        msgFacade.sendByTemplateKey(msgSendDto);
        return R.success(key);
    }

    @Override
    public R<String> sendEmailCode(String email, String templateCode) {
        if (MsgTemplateKey.Email.EMAIL_LOGIN.equals(templateCode)) {
            // 查user表判断重复
            boolean flag = ssoUserService.checkEmail(email, null);
            ArgumentAssert.isTrue(flag, "邮箱尚未注册，请先注册后在登陆。");
        } else if (MsgTemplateKey.Email.EMAIL_REGISTER.equals(templateCode)) {
            // 查user表判断重复
            boolean flag = ssoUserService.checkEmail(email, null);
            ArgumentAssert.isFalse(flag, "该邮箱已经被注册");
        } else if (MsgTemplateKey.Email.EMAIL_EDIT.equals(templateCode)) {
            //查user表判断是否存在
            boolean flag = ssoUserService.checkEmail(email, null);
            ArgumentAssert.isFalse(flag, "该邮箱已经被他人使用");
        }

        String code = RandomUtil.randomNumbers(6);
        String key = RandomUtil.randomNumbers(10);
        CacheKey cacheKey = CaptchaCacheKeyBuilder.build(key, templateCode);
        cacheOps.set(cacheKey, code);

        log.info("邮件验证码 cacheKey={}, code={}", cacheKey, code);


        MsgSendDto msgSendDto = MsgSendMailDto.buildApiSender()
                .addRecipient(email)
                .setTemplateKey(templateCode)
                // code 是在模版中配置的 占位符参数
                .addParam("code", code);

        msgFacade.sendByTemplateKey(msgSendDto);
        return R.success(key);
    }


    @Override
    public CaptchaVo createImg() {
        if (!configFacade.getBoolean(ConfigKey.Console.LOGIN_CAPTCHA_ENABLED, true)) {
            return CaptchaVo.builder().enabled(false).build();
        }

        // 生成验证码图片
        Captcha captcha = graphicCaptchaService.getCaptcha();

        // 缓存验证码
        String key = UUID.fastUUID().toString();
        CacheKey cacheKey = CaptchaCacheKeyBuilder.build(key, GRANT_TYPE);
        cacheOps.set(cacheKey, captcha.text());

        return CaptchaVo.builder()
                .key(key)
                .img(captcha.toBase64())
                .expireTime(cacheKey.getExpire().toSeconds())
                .enabled(true).build();

    }
}
