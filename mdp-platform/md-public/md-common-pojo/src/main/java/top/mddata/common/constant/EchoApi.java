package top.mddata.common.constant;

/**
 * Echo 注解中api的常量
 * <p>
 * 切记，该类下的接口和方法，一定要自己手动创建，否则会注入失败
 * <p>
 * 本类中的 @ua.generator auto insert 请勿删除
 *
 * @author henhen6
 * @since 2020年01月20日11:16:37
 */
public interface EchoApi {
    // @ua.generator auto insert EchoApi

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


}
