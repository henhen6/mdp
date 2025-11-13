package top.mddata.workbench.event;

import org.springframework.context.ApplicationEvent;
import top.mddata.workbench.event.model.LoginStatusDto;

/**
 * 登录事件
 *
 * @author henhen6
 * @date 2020年03月18日17:22:55
 */
public class LoginEvent extends ApplicationEvent {
    public LoginEvent(LoginStatusDto source) {
        super(source);
    }
}
