package top.mddata.workbench.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.wf.captcha.base.Captcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.captcha.graphic.properties.GraphicCaptchaProperties;
import top.mddata.base.captcha.graphic.service.GraphicCaptchaService;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.utils.SpringUtils;
import top.mddata.common.cache.workbench.CaptchaCacheKeyBuilder;
import top.mddata.common.properties.MsgProperties;
import top.mddata.console.message.dto.MsgSendDto;
import top.mddata.console.message.dto.MsgSendMailDto;
import top.mddata.console.message.dto.MsgSendSmsDto;
import top.mddata.console.message.facade.MsgFacade;
import top.mddata.workbench.service.VerificationCodeService;
import top.mddata.workbench.vo.CaptchaVo;

import java.time.Duration;

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
    private final MsgFacade msgFacade;
    private final CacheOps cacheOps;
    private final GraphicCaptchaProperties graphicCaptchaProperties;
    private final MsgProperties msgProperties;

    @Override
    public String sendPhoneCode(String phone, String templateCode) {
        MsgProperties.Sms smsProperties = msgProperties.getSms();
        String code;
        if (MsgProperties.Type.string.equals(smsProperties.getType())) {
            code = RandomUtil.randomString(smsProperties.getLength());
        } else {
            code = RandomUtil.randomNumbers(smsProperties.getLength());
        }

        String key = RandomUtil.randomNumbers(10);
        CacheKey cacheKey = CaptchaCacheKeyBuilder.build(key, templateCode);

        if (smsProperties.getExpirationInMinutes() != null) {
            cacheKey.setExpire(Duration.ofMinutes(smsProperties.getExpirationInMinutes()));
        }
        cacheOps.set(cacheKey, code);

        log.info("短信验证码 cacheKey={}, code={}", cacheKey, code);

        MsgSendDto msgSendDto = MsgSendSmsDto.buildApiSender()
                .addRecipient(phone)
                .setTemplateKey(templateCode)
                // code 是在短信模版中配置的 占位符参数
                .addParam("code", code);

        msgFacade.sendByTemplateKey(msgSendDto);
        return key;
    }

    @Override
    public String sendEmailCode(String email, String templateCode) {
        MsgProperties.Email emailProperties = msgProperties.getEmail();
        String code;
        if (MsgProperties.Type.string.equals(emailProperties.getType())) {
            code = RandomUtil.randomString(emailProperties.getLength());
        } else {
            code = RandomUtil.randomNumbers(emailProperties.getLength());
        }

        String key = RandomUtil.randomNumbers(10);
        CacheKey cacheKey = CaptchaCacheKeyBuilder.build(key, templateCode);
        if (emailProperties.getExpirationInMinutes() != null) {
            cacheKey.setExpire(Duration.ofMinutes(emailProperties.getExpirationInMinutes()));
        }
        cacheOps.set(cacheKey, code);

        log.info("邮件验证码 cacheKey={}, code={}", cacheKey, code);

        MsgSendDto msgSendDto = MsgSendMailDto.buildApiSender()
                .addRecipient(email)
                .setTemplateKey(templateCode)
                // code 是在模版中配置的 占位符参数
                .addParam("code", code)
                .addParam("validityPeriod", String.valueOf(cacheKey.getExpire().toMinutes()));

        msgFacade.sendByTemplateKey(msgSendDto);
        return key;
    }


    @Override
    public CaptchaVo createImg() {
        if (!graphicCaptchaProperties.getEnabled()) {
            return CaptchaVo.builder().enabled(false).build();
        }

        // 生成验证码图片
        Captcha captcha = SpringUtils.getBean(GraphicCaptchaService.class).getCaptcha();

        // 缓存验证码
        String key = UUID.fastUUID().toString();
        CacheKey cacheKey = CaptchaCacheKeyBuilder.build(key, GRANT_TYPE);
        cacheOps.set(cacheKey, captcha.text());
        log.info("图片验证码 cacheKey={}, code={}", cacheKey, captcha.text());

        return CaptchaVo.builder()
                .key(key)
                .img(captcha.toBase64())
                .expireTime(cacheKey.getExpire().toSeconds())
                .enabled(true).build();

    }
}
