package top.mddata.common.constant;

/**
 * Echo 注解中api的常量
 * <p>
 * 切记，该类下的接口和方法，一定要自己手动创建，否则会注入失败
 * <p>
 * 本类中的 @md.generator auto insert 请勿删除
 *
 * @author henhen6
 * @since 2020年01月20日11:16:37
 */
public interface EchoApi {
    // @md.generator auto insert EchoApi

    /**
     * 数据字典项 feign查询类 全类名
     */
    String DICT_CLASS = "dictFacadeImpl";
    /**
     * 组织 回显
     */
    String ORG_CLASS = "orgFacadeImpl";
    /**
     * 岗位 回显
     */
    String POSITION_CLASS = "positionFacadeImpl";
    /**
     * 接口调用日志 回显
     */
    String API_CALL_LOG_CLASS = "apiCallLogEchoImpl";
    /**
     * 回调任务 回显
     */
    String NOTIFY_INFO_CLASS = "notifyInfoEchoImpl";
    /**
     * 事件推送 回显
     */
    String EVENT_PUSH_CLASS = "eventPushEchoImpl";


}
