//package top.mddata.open.manage.facade.impl;
//
//import top.mddata.open.manage.facade.OpOauthUnionidFacade;
//import top.mddata.open.manage.service.OpOauthUnionidService;
//import top.mddata.open.manage.vo.OpOauthUnionidVo;
//import top.mddata.base.base.R;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
///**
// *
// * @author henhen6
// * @since 2025/8/22 00:07
// */
//@Component
//public class OpOauthUnionidFacadeImpl implements OpOauthUnionidFacade {
//    @Autowired
//    private OpOauthUnionidService opOauthUnionidService;
//
//    @Override
//    public R<OpOauthUnionidVo> getBySubjectIdAndUserId(Long subjectId, Long userId) {
//        return R.success(opOauthUnionidService.getBySubjectIdAndUserId(subjectId, userId));
//    }
//}
