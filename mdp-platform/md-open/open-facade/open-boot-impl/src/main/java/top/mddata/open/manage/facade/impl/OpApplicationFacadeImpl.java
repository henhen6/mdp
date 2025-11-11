//package top.mddata.open.manage.facade.impl;
//
//import top.mddata.open.manage.facade.OpApplicationFacade;
//import top.mddata.open.manage.service.OpApplicationService;
//import top.mddata.open.manage.service.OpOauthScopeService;
//import top.mddata.open.manage.vo.OpApplicationVo;
//import top.mddata.open.manage.vo.OpOauthScopeVo;
//import top.mddata.base.base.R;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
///**
// * 应用管理单体版实现类
// *
// * @author henhen6
// * @since 2025/8/12 11:28
// */
//@Service
//public class OpApplicationFacadeImpl implements OpApplicationFacade {
//    @Autowired
//    private OpApplicationService opApplicationService;
//    @Autowired
//    private OpOauthScopeService opOauthScopeService;
//
//    @Override
//    public R<List<OpApplicationVo>> listNeedPushApplication() {
//        return R.success(opApplicationService.listNeedPushApplication());
//    }
//
//    @Override
//    public R<OpApplicationVo> getOpApplicationByAppId(String appId) {
//        return R.success(opApplicationService.getOpApplicationByAppId(appId));
//    }
//
//    @Override
//    public R<List<OpOauthScopeVo>> getScopeListByCode(List<String> scopes) {
//        return R.success(opOauthScopeService.getScopeListByCode(scopes));
//    }
//
//    @Override
//    public R<OpApplicationVo> getByAppId(String appId) {
//        return R.success(opApplicationService.getByAppId(appId));
//    }
//}
