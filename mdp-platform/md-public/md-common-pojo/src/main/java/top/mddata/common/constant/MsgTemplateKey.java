package top.mddata.common.constant;

/**
 * 模版消息 唯一Key
 * @author henhen
 * @since 2025/12/22 21:35
 */
public interface MsgTemplateKey {
    interface Notice {
        // 后台发送站内信特殊模版
        String NOTICE = "NOTICE";
    }

    interface Sms {
        // 手机注册
        String PHONE_REGISTER = "PHONE_REGISTER";
        // 手机登录
        String PHONE_LOGIN = "PHONE_LOGIN";
        // 安全设置 修改个人手机号
        String PHONE_EDIT = "PHONE_EDIT";
    }

    interface Email {
        // 安全设置 修改个人邮箱
        String EMAIL_EDIT = "EMAIL_EDIT";
        // 邮箱注册
        String EMAIL_REGISTER = "EMAIL_REGISTER";
        // 邮箱登录
        String EMAIL_LOGIN = "EMAIL_LOGIN";
    }
}
