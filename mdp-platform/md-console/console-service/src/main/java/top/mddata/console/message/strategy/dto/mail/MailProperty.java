package top.mddata.console.message.strategy.dto.mail;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.mddata.console.message.strategy.dto.BaseProperty;

/**
 * 接口中需要配置的 邮件属性
 * @author henhen
 * @date 2022/7/10 0010 18:56
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MailProperty extends BaseProperty {
    /**
     * 端口号
     * */
    private String port;

    /**
     * 发件人地址
     * */
    private String fromAddress;

    /**
     * 发送人昵称
     * */
    private String nickName;

    /**
     * 服务器地址
     * */
    private String smtpServer;

    /**
     * 账号
     * */
    private String username;

    /**
     * 密码
     * */
    private String password;

    /**
     * 是否开启ssl 默认开启
     * */
    private String isSsl;

    /**
     * 是否开启验证 默认开启
     * */
    private String isAuth;

    /**
     * 重试间隔（单位：秒），默认为5秒
     */
    private int retryInterval = 5;

    /**
     * 重试次数，默认为1次
     */
    private int maxRetries = 1;

}
