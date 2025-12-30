package top.mddata.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import top.mddata.base.constant.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息配置
 *
 * @author henhen6
 * @date 2025年12月28日23:04:38
 */
@Setter
@Getter
@RefreshScope
@ConfigurationProperties(prefix = MsgProperties.PREFIX)
public class MsgProperties {
    public static final String PREFIX = Constants.PROJECT_PREFIX + ".msg";

    /**
     * 消息模板通用参数
     */
    private Map<String, String> param = new HashMap<>();

    private Sms sms = new Sms();
    private Email email = new Email();

    public enum Type {
        /** 数字 */
        number,
        /** 字符串 */
        string;
    }

    @Getter
    @Setter
    public static class Sms {
        /**
         * 验证码类型
         */
        private Type type = Type.number;
        /**
         * 内容长度
         */
        private int length = 6;
        /**
         * 过期时间
         */
        private Long expirationInMinutes;
    }

    @Getter
    @Setter
    public static class Email {
        /**
         * 验证码类型
         */
        private Type type = Type.string;
        /**
         * 内容长度
         */
        private int length = 6;
        /**
         * 过期时间
         */
        private Long expirationInMinutes;
    }
}