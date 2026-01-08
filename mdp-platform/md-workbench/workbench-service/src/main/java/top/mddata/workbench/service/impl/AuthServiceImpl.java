package top.mddata.workbench.service.impl;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.dev33.satoken.temp.SaTempUtil;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
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
import top.mddata.common.entity.OrgNature;
import top.mddata.common.entity.User;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.properties.SystemProperties;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.dto.LoginLogDto;
import top.mddata.workbench.dto.RegisterByEmailDto;
import top.mddata.workbench.dto.RegisterByPhoneDto;
import top.mddata.workbench.event.LoginEvent;
import top.mddata.workbench.handler.LoginStrategy;
import top.mddata.workbench.service.AuthService;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.vo.LoginVo;

import java.util.List;
import java.util.Map;

import static top.mddata.base.constant.ContextConstants.COMPANY_ID;
import static top.mddata.base.constant.ContextConstants.COMPANY_NATURE;
import static top.mddata.base.constant.ContextConstants.DEPT_ID;
import static top.mddata.base.constant.ContextConstants.TOP_COMPANY_ID;
import static top.mddata.base.constant.ContextConstants.TOP_COMPANY_IS_ADMIN;
import static top.mddata.base.constant.ContextConstants.TOP_COMPANY_NATURE;
import static top.mddata.base.constant.ContextConstants.USER_ID;


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
    private final SystemProperties systemProperties;
    private final SaTokenConfig saTokenConfig;
    private final CacheOps cacheOps;
    private final SsoUserService ssoUserService;
    private final Map<String, LoginStrategy> loginStrategy;

    @Override
    public R<LoginVo> login(LoginDto login) {
        // 校验参数
        LoginStrategy strategy = loginStrategy.get(login.getAuthType().name());
        strategy.checkParam(login);

        // 查找用户
        User ssoUser = strategy.getUser(login.getUsername());

//        判断密码
        strategy.checkUserPassword(login, ssoUser);

        // 5. 检查用户状态
        strategy.checkUserState(login, ssoUser);

        //        TODO 判断用户是否可以登录该应用


        Long userId = ssoUser.getId();

//        查询用户部门信息
        TempOrg org = findOrg(ssoUser);

        // 创建Account-Session
//        StpUtil.login(userId, "PC");
        StpUtil.login(userId, new SaLoginParameter()
                .setDeviceType("PC")
                .setDeviceId(SaFoxUtil.getRandomString(32)));

        SaSession session = StpUtil.getSession();
        session.setLoginId(ssoUser.getId());
        if (org.getCurrentTopCompanyId() != null) {
            session.set(TOP_COMPANY_ID, org.getCurrentTopCompanyId());
        } else {
            session.delete(TOP_COMPANY_ID);
        }
        if (org.getCurrentTopCompanyNature() != null) {
            session.set(TOP_COMPANY_NATURE, org.getCurrentTopCompanyNature());
        } else {
            session.delete(TOP_COMPANY_NATURE);
        }
        if (org.getCurrentCompanyNature() != null) {
            session.set(COMPANY_NATURE, org.getCurrentCompanyNature());
        } else {
            session.delete(COMPANY_NATURE);
        }
        if (org.getCurrentCompanyId() != null) {
            session.set(COMPANY_ID, org.getCurrentCompanyId());
        } else {
            session.delete(COMPANY_ID);
        }
        if (org.getCurrentDeptId() != null) {
            session.set(DEPT_ID, org.getCurrentDeptId());
        } else {
            session.delete(DEPT_ID);
        }

        session.set(TOP_COMPANY_IS_ADMIN, org.isCurrentTopCompanyIsAdmin());


        // 封装返回值
        JSONObject obj = new JSONObject();
        obj.put(USER_ID, ssoUser.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        LoginVo loginVO = BeanUtil.toBean(tokenInfo, LoginVo.class);
        loginVO.setExpire(tokenInfo.getTokenTimeout());
        loginVO.setRefreshToken(SaTempUtil.createToken(obj.toString(), 2 * saTokenConfig.getTimeout()));
        loginVO.setId(userId);

        // 发送登录成功事件
        LoginLogDto dto = LoginLogDto.success(login.getAuthType(), login.getDeviceInfo(), login.getUsername(), "登录成功", JSON.toJSONString(tokenInfo));
        SpringUtils.publishEvent(new LoginEvent(dto));

        return R.success(loginVO);
    }

    private TempOrg findOrg(User sysUser) {
        // 当前所属部门
        Long currentDeptId = null;
        // 当前所属单位
        Long currentCompanyId = null;
        Integer currentCompanyNature = OrgNatureEnum.DEFAULT.getCode();
        // 当前所属顶级单位
        Long currentTopCompanyId = null;
        Integer currentTopCompanyNature = OrgNatureEnum.DEFAULT.getCode();
//        当前顶级组织是否超级管理
        boolean currentTopCompanyIsAdmin = false;


        if (sysUser == null) {
            return TempOrg.builder()
                    .currentTopCompanyId(currentTopCompanyId)
                    .currentTopCompanyNature(currentTopCompanyNature)
                    .currentCompanyId(currentCompanyId)
                    .currentCompanyNature(currentCompanyNature)
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

        // 查询单位的组织性质
        if (defaultCompany != null) {
            OrgNature orgNature = ssoUserService.getOrgNatureByOrgId(defaultCompany.getId());
            if (orgNature != null) {
                currentCompanyNature = orgNature.getNature();
            }
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

        // 查询顶级单位的组织性质
        if (rootCompany != null) {
            OrgNature orgNature = ssoUserService.getOrgNatureByOrgId(rootCompany.getId());
            if (orgNature != null) {
                currentTopCompanyNature = orgNature.getNature();
            }
        }

        ssoUserService.updateById(updateUser);

        // 组织性质拥有 99，就视为组织是超级管理员
        if (rootCompany != null) {
            currentTopCompanyIsAdmin = ssoUserService.getTopCompanyIsAdminById(rootCompany.getId());
        }

        return TempOrg.builder()
                .currentTopCompanyNature(currentTopCompanyNature)
                .currentTopCompanyId(currentTopCompanyId)
                .currentCompanyNature(currentCompanyNature)
                .currentCompanyId(currentCompanyId)
                .currentDeptId(currentDeptId)
                .currentTopCompanyIsAdmin(currentTopCompanyIsAdmin)
                .build();
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
         * 当前公司性质
         */
        private Integer currentCompanyNature;
        /**
         * 当前顶级公司id
         */
        private Long currentTopCompanyId;
        /**
         * 当前顶级公司性质
         */
        private Integer currentTopCompanyNature;
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
