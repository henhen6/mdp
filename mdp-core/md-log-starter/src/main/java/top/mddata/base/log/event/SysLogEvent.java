package top.mddata.base.log.event;


import top.mddata.base.model.log.OptLogDTO;
import org.springframework.context.ApplicationEvent;

/**
 * 系统日志事件
 *
 * @author henhen6
 * @date 2019-07-01 15:13
 */
public class SysLogEvent extends ApplicationEvent {

    public SysLogEvent(OptLogDTO source) {
        super(source);
    }
}
