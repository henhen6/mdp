//package top.mddata.open.manage.facade.impl;
//
//import top.mddata.open.manage.facade.OpScopeFacade;
//import top.mddata.open.manage.service.OpOauthScopeService;
//import top.mddata.open.manage.vo.OpOauthScopeVo;
//import top.mddata.base.base.R;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
///**
// *
// * @author henhen6
// * @since 2025/8/24 23:43
// */
//@Service
//public class OpScopeFacadeImpl implements OpScopeFacade {
//    @Autowired
//    private OpOauthScopeService opOauthScopeService;
//
//    @Override
//    public R<List<OpOauthScopeVo>> listByApplicationId(Long applicationId) {
//        return R.success(opOauthScopeService.listByApplicationId(applicationId));
//    }
//}
