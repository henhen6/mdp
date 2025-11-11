//package top.mddata.open.manage.facade;
//
//import top.mddata.open.manage.vo.OpApplicationVo;
//import top.mddata.open.manage.vo.OpOauthScopeVo;
//import top.mddata.base.base.R;
//
//import java.util.List;
//
///**
// * 应用接口
// *
// * @author henhen6
// * @since 2025/8/12 11:24
// */
//public interface OpApplicationFacade {
//    /**
//     * 查询需要 接收事件推送的应用
//     *
//     * @return 应用
//     */
//    R<List<OpApplicationVo>> listNeedPushApplication();
//
//
//    /**
//     * 根据应用ID查询应用
//     *
//     * @param appId 应用ID
//     * @return 应用
//     */
//    R<OpApplicationVo> getOpApplicationByAppId(String appId);
//
//    /**
//     * 根据权限编码查询 应用权限
//     *
//     * @param scopes 权限编码
//     * @return 应用权限
//     */
//    R<List<OpOauthScopeVo>> getScopeListByCode(List<String> scopes);
//
//    /**
//     * 根据id查询应用
//     *
//     * @param id ID
//     * @return 应用
//     */
//    R<OpApplicationVo> getByAppId(String id);
//
//}
