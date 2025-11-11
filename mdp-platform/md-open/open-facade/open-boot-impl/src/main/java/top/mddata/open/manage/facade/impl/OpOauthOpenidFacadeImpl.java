//package top.mddata.open.manage.facade.impl;
//
//import top.mddata.open.manage.facade.OpOauthOpenidFacade;
//import top.mddata.open.manage.service.OpOauthOpenidService;
//import top.mddata.open.manage.vo.OpOauthOpenidVo;
//import top.mddata.base.base.R;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
///**
// *
// * @author henhen6
// * @since 2025/8/22 12:43
// */
//@Service
//public class OpOauthOpenidFacadeImpl implements OpOauthOpenidFacade {
//    @Autowired
//    private OpOauthOpenidService opOauthOpenidService;
//
//    @Override
//    public R<OpOauthOpenidVo> getByAppIdAndUserId(String appId, Long userId) {
//        return R.success(opOauthOpenidService.getByAppIdAndUserId(appId, userId));
//    }
//}
