package top.mddata.workbench.service.impl;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.temp.SaTempUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSONObject;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.base.utils.SpringUtils;
import top.mddata.common.cache.workbench.CaptchaCacheKeyBuilder;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.User;
import top.mddata.common.properties.SystemProperties;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.RegisterByEmailDto;
import top.mddata.workbench.dto.RegisterByPhoneDto;
import top.mddata.workbench.enumeration.MsgTemplateCodeEnum;
import top.mddata.workbench.event.LoginEvent;
import top.mddata.workbench.event.model.LoginStatusDto;
import top.mddata.workbench.service.AuthService;
import top.mddata.workbench.service.LoginStrategy;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.vo.LoginVo;

import java.util.List;
import java.util.Map;

import static top.mddata.base.constant.ContextConstants.JWT_KEY_COMPANY_ID;
import static top.mddata.base.constant.ContextConstants.JWT_KEY_DEPT_ID;
import static top.mddata.base.constant.ContextConstants.JWT_KEY_TOP_COMPANY_ID;
import static top.mddata.base.constant.ContextConstants.JWT_KEY_TOP_COMPANY_IS_ADMIN;
import static top.mddata.base.constant.ContextConstants.JWT_KEY_USER_ID;


/**
 * 认证
 *
 * @author henhen6
 * @since 2025/6/30 16:41
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final SsoUserService ssoUserService;
    private final SystemProperties systemProperties;
    private final SaTokenConfig saTokenConfig;
    private final CacheOps cacheOps;

    private final Map<String, LoginStrategy> loginStrategy;

    @Override
    public R<LoginVo> login(LoginDto login) {
        // 校验参数
        LoginStrategy strategy = loginStrategy.get(login.getLoginType().name());
        strategy.checkParam(login);

        // 查找用户
        User ssoUser = strategy.getUser(login.getUsername());

//        判断密码
        strategy.checkUserPassword(login, ssoUser);

        // 5. 检查用户状态
        strategy.checkUserState(ssoUser);

        //        TODO 判断用户是否可以登录该应用


        Long userId = ssoUser.getId();

//        查询用户部门信息
        TempOrg org = findOrg(ssoUser);

        // 创建Account-Session
        StpUtil.login(userId, "PC");
        SaSession session = StpUtil.getSession();
        session.setLoginId(ssoUser.getId());
        if (org.getCurrentTopCompanyId() != null) {
            session.set(JWT_KEY_TOP_COMPANY_ID, org.getCurrentTopCompanyId());
        } else {
            session.delete(JWT_KEY_TOP_COMPANY_ID);
        }
        if (org.getCurrentCompanyId() != null) {
            session.set(JWT_KEY_COMPANY_ID, org.getCurrentCompanyId());
        } else {
            session.delete(JWT_KEY_COMPANY_ID);
        }
        if (org.getCurrentDeptId() != null) {
            session.set(JWT_KEY_DEPT_ID, org.getCurrentDeptId());
        } else {
            session.delete(JWT_KEY_DEPT_ID);
        }
        session.set(JWT_KEY_TOP_COMPANY_IS_ADMIN, org.isCurrentTopCompanyIsAdmin());

        // 发送登录成功事件
        LoginStatusDto loginStatus = LoginStatusDto.success(ssoUser.getId());
        SpringUtils.publishEvent(new LoginEvent(loginStatus));

        // 封装返回值
        JSONObject obj = new JSONObject();
        obj.put(JWT_KEY_USER_ID, ssoUser.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        LoginVo loginVO = BeanUtil.toBean(tokenInfo, LoginVo.class);
        loginVO.setExpire(tokenInfo.getTokenTimeout());
        loginVO.setRefreshToken(SaTempUtil.createToken(obj.toString(), 2 * saTokenConfig.getTimeout()));
        loginVO.setId(userId);
        return R.success(loginVO);
    }

    private TempOrg findOrg(User sysUser) {
        // 当前所属部门
        Long currentDeptId = null;
        // 当前所属单位
        Long currentCompanyId = null;
        // 当前所属顶级单位
        Long currentTopCompanyId = null;
//        当前顶级组织是否超级管理
        boolean currentTopCompanyIsAdmin = false;


        if (sysUser == null) {
            return TempOrg.builder()
                    .currentTopCompanyId(currentTopCompanyId)
                    .currentCompanyId(currentCompanyId)
                    .currentDeptId(currentDeptId).currentTopCompanyIsAdmin(currentTopCompanyIsAdmin).build();
        }
        Long userId = sysUser.getId();

        User updateUser = UpdateEntity.of(User.class, userId);

//            查最后一次登录时 所属部门
        if (sysUser.getLastDeptId() == null) {
            // 上次登录部门为空，则随机选择一个部门
            List<Org> deptList = ssoUserService.findDeptByUserId(userId, null);
            Org defaultDept = ssoUserService.getDefaultOrg(deptList, null);

            currentDeptId = defaultDept != null ? defaultDept.getId() : null;
            updateUser.setLastDeptId(currentDeptId);

        } else {
            currentDeptId = sysUser.getLastDeptId();
        }

//            查最后一次登录时 所属单位
        Org defaultCompany;
        if (sysUser.getLastCompanyId() == null) {
            if (currentDeptId != null) {
                defaultCompany = ssoUserService.getCompanyByDeptId(currentDeptId);
            } else {
                // currentDeptId 为空，员工可能直接挂在单位下、也可能不属于任何部门
                List<Org> companyList = ssoUserService.findCompanyByUserId(userId);
                defaultCompany = ssoUserService.getDefaultOrg(companyList, sysUser.getLastCompanyId());
            }

            currentCompanyId = defaultCompany != null ? defaultCompany.getId() : null;
            updateUser.setLastCompanyId(currentCompanyId);
        } else {
            currentCompanyId = sysUser.getLastCompanyId();
            defaultCompany = ssoUserService.getOrgByIdCache(currentCompanyId);
        }

        // 查最后一次登录时 所属顶级单位
        Org rootCompany = null;
        if (sysUser.getLastTopCompanyId() == null) {
            if (defaultCompany != null) {
                Long rootId = MyTreeUtil.getTopNodeId(defaultCompany.getTreePath());
                if (rootId != null) {
                    rootCompany = ssoUserService.getOrgByIdCache(rootId);
                } else {
                    rootCompany = defaultCompany;
                }
            }
            currentTopCompanyId = rootCompany != null ? rootCompany.getId() : null;
            updateUser.setLastTopCompanyId(currentTopCompanyId);
        } else {
            currentTopCompanyId = sysUser.getLastTopCompanyId();
            rootCompany = ssoUserService.getOrgByIdCache(currentTopCompanyId);
        }

        ssoUserService.updateById(updateUser);


        // 组织性质拥有 99，就视为组织是超级管理员
        if (rootCompany != null) {
            currentTopCompanyIsAdmin = ssoUserService.getTopCompanyIsAdminById(rootCompany.getId());
        }

        return TempOrg.builder()
                .currentTopCompanyId(currentTopCompanyId)
                .currentCompanyId(currentCompanyId)
                .currentDeptId(currentDeptId)
                .currentTopCompanyIsAdmin(currentTopCompanyIsAdmin)
                .build();
    }


    @Override
    public R<String> sendPhoneCode(String phone, String templateCode) {
        if (MsgTemplateCodeEnum.PHONE_REGISTER.eq(templateCode)) {
            // 查user表判断重复
            boolean flag = ssoUserService.checkPhone(phone, null);
            ArgumentAssert.isFalse(flag, "该手机号已经被注册");
        } else if (MsgTemplateCodeEnum.PHONE_LOGIN.eq(templateCode)) {
            //查user表判断是否存在
            boolean flag = ssoUserService.checkPhone(phone, null);
            ArgumentAssert.isTrue(flag, "该手机号尚未注册，请先注册后在登陆。");
        } else if (MsgTemplateCodeEnum.PHONE_EDIT.eq(templateCode)) {
            //查user表判断是否存在
            boolean flag = ssoUserService.checkPhone(phone, null);
            ArgumentAssert.isFalse(flag, "该手机号已经被他人使用");
        }

        String code = RandomUtil.randomNumbers(6);
        String key = RandomUtil.randomNumbers(10);
        CacheKey cacheKey = CaptchaCacheKeyBuilder.build(key, templateCode);
        cacheOps.set(cacheKey, code);

        log.info("短信验证码 cacheKey={}, code={}", cacheKey, code);

        // TODO 未完成
        // 配置一个「模板标识」为 templateCode， 且「模板内容」中需要有 code 占位符
        // 也可以考虑给模板增加一个过期时间等参数
//        ExtendMsgSendVO msgSendVO = ExtendMsgSendVO.builder().code(templateCode).build();
//        msgSendVO.addParam("code", code);
//        msgSendVO.addRecipient(mobile);
//        return R.success(msgFacade.sendByTemplate(msgSendVO));
        return R.success(key);
    }

    @Override
    public R<String> sendEmailCode(String email, String templateCode) {
        if (MsgTemplateCodeEnum.EMAIL_LOGIN.eq(templateCode)) {
            // 查user表判断重复
            boolean flag = ssoUserService.checkEmail(email, null);
            ArgumentAssert.isTrue(flag, "邮箱尚未注册，请先注册后在登陆。");
        } else if (MsgTemplateCodeEnum.EMAIL_REGISTER.eq(templateCode)) {
            // 查user表判断重复
            boolean flag = ssoUserService.checkEmail(email, null);
            ArgumentAssert.isFalse(flag, "该邮箱已经被注册");
        } else if (MsgTemplateCodeEnum.EMAIL_EDIT.eq(templateCode)) {
            //查user表判断是否存在
            boolean flag = ssoUserService.checkEmail(email, null);
            ArgumentAssert.isFalse(flag, "该邮箱已经被他人使用");
        }

        String code = RandomUtil.randomNumbers(6);
        String key = RandomUtil.randomNumbers(10);
        CacheKey cacheKey = CaptchaCacheKeyBuilder.build(key, templateCode);
        cacheOps.set(cacheKey, code);

        log.info("邮件验证码 cacheKey={}, code={}", cacheKey, code);

        // TODO 未完成
        // 配置一个「模板标识」为 templateCode， 且「模板内容」中需要有 code 占位符
//        ExtendMsgSendVO msgSendVO = ExtendMsgSendVO.builder().code(templateCode).build();
//        msgSendVO.addParam("code", code);
//        msgSendVO.addRecipient(email);
//        return R.success(msgFacade.sendByTemplate(msgSendVO));
        return R.success(key);
    }


    @Override
    public String registerByEmail(RegisterByEmailDto register) {
        if (systemProperties.getVerifyCaptcha()) {
            CacheKey cacheKey = new CaptchaCacheKeyBuilder().key(register.getEmail(), register.getKey());
            CacheResult<String> code = cacheOps.get(cacheKey);
            ArgumentAssert.equals(code.getValue(), register.getCode(), "验证码不正确");
        }
        User defUser = BeanUtil.toBean(register, User.class);

        ssoUserService.registerByEmail(defUser);

        return defUser.getEmail();
    }

    @Override
    public String registerByPhone(RegisterByPhoneDto register) {
        if (systemProperties.getVerifyCaptcha()) {
            CacheKey cacheKey = new CaptchaCacheKeyBuilder().key(register.getPhone(), register.getKey());
            CacheResult<String> code = cacheOps.get(cacheKey);
            ArgumentAssert.equals(code.getValue(), register.getCode(), "验证码不正确");
        }
        User defUser = BeanUtil.toBean(register, User.class);

        ssoUserService.registerByPhone(defUser);

        return defUser.getPhone();
    }

    @Builder
    @AllArgsConstructor
    @Getter
    private static class TempOrg {
        /**
         * 当前公司id
         */
        private Long currentCompanyId;
        /**
         * 当前顶级公司id
         */
        private Long currentTopCompanyId;
        /**
         * 当前部门id
         */
        private Long currentDeptId;
        /**
         * 当前顶级公司是否是超管企业
         */
        private boolean currentTopCompanyIsAdmin;
    }
}
