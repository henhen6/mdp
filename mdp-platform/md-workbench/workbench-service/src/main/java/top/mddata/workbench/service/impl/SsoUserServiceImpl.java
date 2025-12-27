package top.mddata.workbench.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.LambdaGetter;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.email.jakarta.api.MailClient;
import org.dromara.email.jakarta.comm.entity.MailMessage;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.chuanglan.config.ChuangLanConfig;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.cache.redis.CacheResult;
import top.mddata.base.cache.repository.CacheOps;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.ContextUtil;
import top.mddata.base.utils.DateUtils;
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.common.cache.console.organization.OrgCacheKeyBuilder;
import top.mddata.common.cache.console.organization.UserCacheKeyBuilder;
import top.mddata.common.cache.console.organization.UserOrgCacheKeyBuilder;
import top.mddata.common.cache.workbench.CaptchaCacheKeyBuilder;
import top.mddata.common.cache.workbench.SsoUserEmailCacheKeyBuilder;
import top.mddata.common.cache.workbench.SsoUserPhoneCacheKeyBuilder;
import top.mddata.common.cache.workbench.SsoUserUserNameCacheKeyBuilder;
import top.mddata.common.constant.ConfigKey;
import top.mddata.common.constant.FileObjectType;
import top.mddata.common.constant.MsgTemplateKey;
import top.mddata.common.entity.Org;
import top.mddata.common.entity.OrgNature;
import top.mddata.common.entity.User;
import top.mddata.common.enumeration.BooleanEnum;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.organization.OrgTypeEnum;
import top.mddata.common.mapper.OrgMapper;
import top.mddata.common.mapper.OrgNatureMapper;
import top.mddata.common.mapper.UserMapper;
import top.mddata.console.system.dto.RelateFilesToBizDto;
import top.mddata.console.system.facade.ConfigFacade;
import top.mddata.console.system.facade.FileFacade;
import top.mddata.workbench.dto.ProfileEmailDto;
import top.mddata.workbench.dto.ProfilePasswordDto;
import top.mddata.workbench.dto.ProfilePhoneDto;
import top.mddata.workbench.dto.ProfileUserDto;
import top.mddata.workbench.service.SsoUserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * 用户 服务层实现。
 *
 * @author henhen6
 * @since 2025-07-09 00:46:37
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SsoUserServiceImpl extends SuperServiceImpl<UserMapper, User> implements SsoUserService {
    private final OrgMapper orgMapper;
    private final OrgNatureMapper orgNatureMapper;
    private final FileFacade fileFacade;
    private final CacheOps cacheOps;
    private final ConfigFacade configFacade;
    private final ChuangLanConfig chuangLanConfig;
    private final MailClient mailClient;

    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new UserCacheKeyBuilder();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateProfile(ProfileUserDto dto) {
        ArgumentAssert.isTrue(dto.getId().equals(ContextUtil.getUserId()), "只能修改个人信息");
        User entity = UpdateEntity.of(User.class, dto.getId());
        BeanUtil.copyProperties(dto, entity);
        entity.setAvatar(entity.getId());

        updateById(entity);
        delCache(entity.getId());

        // 关联附件 注意：dto.logo 是前端传递过来的文件id， entity.logo 是在存入数据库前，设置的唯一对象id（为了节约雪花id，可以复用entity.getId(), 也可生成新的唯一id）
        fileFacade.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getAvatar())
                .objectType(FileObjectType.Console.USER_AVATAR)
                .build().setKeepFileIds(dto.getAvatar()));
        return entity.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        CacheKey key = SsoUserUserNameCacheKeyBuilder.builder(username);
        return getDefUser(key, username, User::getUsername);
    }

    @Override
    @Transactional(readOnly = true)
    public User getByPhone(String phone) {
        CacheKey key = SsoUserPhoneCacheKeyBuilder.builder(phone);
        return getDefUser(key, phone, User::getPhone);
    }

    @Override
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        CacheKey key = SsoUserEmailCacheKeyBuilder.builder(email);
        return getDefUser(key, email, User::getEmail);
    }

    private User getDefUser(CacheKey key, String value, LambdaGetter<User> fun) {
        CacheResult<Long> result = cacheOps.get(key, k -> {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq(fun, value);
            User defUser = getOne(queryWrapper);
            return defUser != null ? defUser.getId() : null;
        });
        return getByIdCache(Convert.toLong(result.getValue()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPwErrorNum(Long id) {
        getMapper().resetPwErrorNum(id, LocalDateTime.now());
        delCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrPwErrorNumById(Long id) {
        getMapper().incrPwErrorNumById(id, LocalDateTime.now());
        delCache(id);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean checkPhone(String phone, Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getPhone, phone).ne(User::getId, id);
        return getMapper().selectCountByQuery(queryWrapper) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkEmail(String email, Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getEmail, email).ne(User::getId, id);
        return getMapper().selectCountByQuery(queryWrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerByEmail(User ssoUser) {
        ArgumentAssert.isFalse(checkEmail(ssoUser.getEmail(), null), "邮箱：{}已经存在", ssoUser.getEmail());
        initSsoUser(ssoUser);
        ssoUser.setUsername(ssoUser.getEmail());

        save(ssoUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerByPhone(User ssoUser) {
        ArgumentAssert.isFalse(checkPhone(ssoUser.getPhone(), null), "手机号：{}已经存在", ssoUser.getPhone());
        initSsoUser(ssoUser);
        ssoUser.setUsername(ssoUser.getPhone());
        save(ssoUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Org> findDeptByUserId(Long userId, Long companyId) {
        // 员工所属的机构 ID （可能含有单位或部门）
        List<Long> orgIdList = findOrgIdByUserId(userId);
        if (CollUtil.isEmpty(orgIdList)) {
            return Collections.emptyList();
        }
        // 员工所属的机构 实体类
        List<Org> orgList = orgMapper.selectListByIds(orgIdList);

        /*
         * 有可能 companyId 为空，但 orgIdList 不为空
         * 原因： 在维护机构数据时， 没有将 部门 挂在 单位 下，而是直接将 部门 作为根节点，并挂载 子部门。
         */

        return orgList.stream()
                // 只查找部门
                .filter(item -> OrgTypeEnum.DEPT.eq(item.getOrgType()))
                // 限定查找 companyId 的下级部门
                .filter(item -> companyId == null || StrUtil.contains(item.getTreePath(), MyTreeUtil.buildTreePath(companyId)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Org getDefaultOrg(List<Org> orgList, Long lastOrgId) {
        if (CollUtil.isEmpty(orgList)) {
            return null;
        }
        Org sysOrg = null;
        if (lastOrgId != null) {
            sysOrg = orgList.stream().filter(item -> lastOrgId.equals(item.getId())).findFirst().orElse(null);
        }
        if (sysOrg == null && !orgList.isEmpty()) {
            sysOrg = orgList.get(0);
        }
        return sysOrg;
    }

    @Override
    @Transactional(readOnly = true)
    public Org getCompanyByDeptId(Long deptId) {
        if (deptId == null) {
            return null;
        }
        Org org = getOrgByIdCache(deptId);
        if (org == null) {
            return null;
        }
        if (OrgTypeEnum.COMPANY.eq(org.getOrgType())) {
            return org;
        }
        return getCompanyByDeptId(org.getParentId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Org> findCompanyByUserId(Long userId) {
        // 下文中提到的机构：指 base_org 中的数据，无论它的 type 为单位或部门

        // 员工所属的机构 ID
        List<Long> orgIdList = findOrgIdByUserId(userId);
        if (CollUtil.isEmpty(orgIdList)) {
            return Collections.emptyList();
        }
        // 员工所属的机构 实体类
        List<Org> orgList = orgMapper.selectListByIds(orgIdList);

        // 员工所属的机构的所有上级ID
        List<Long> parentIdList = orgList.stream()
                .map(item -> StrUtil.splitToArray(item.getTreePath(), MyTreeUtil.TREE_SPLIT))
                // 数组流 转 字符串流
                .flatMap(Arrays::stream)
                .distinct()
                // 去除空数据
                .filter(ObjectUtil::isNotEmpty)
                .map(Convert::toLong)
                // 类型转换
                .toList();

        // 员工所属的机构 以及 上级机构
        if (CollUtil.isEmpty(parentIdList)) {
            return Collections.emptyList();
        }
        List<Org> sysOrgList = orgMapper.selectListByIds(parentIdList);

        // 员工所属的 单位或上级单位
        List<Org> companyList = new ArrayList<>();
        Set<Long> companyIdSet = new HashSet<>();
        for (Org sysOrg : sysOrgList) {
            if (OrgTypeEnum.COMPANY.eq(sysOrg.getOrgType()) && !companyIdSet.contains(sysOrg.getId())) {
                companyList.add(sysOrg);
                companyIdSet.add(sysOrg.getId());
            }
        }
        return companyList;
    }

    private List<Long> findOrgIdByUserId(Long userId) {
        CacheKey eoKey = UserOrgCacheKeyBuilder.build(userId);
        CacheResult<List<Long>> orgIdResult = cacheOps.get(eoKey, k -> orgMapper.selectOrgByUserId(userId));
        return orgIdResult.asList();
    }

    @Override
    @Transactional(readOnly = true)
    public Org getOrgByIdCache(Long id) {
        if (id == null) {
            return null;
        }
        CacheKey cacheKey = OrgCacheKeyBuilder.build(id);
        CacheResult<Org> result = cacheOps.get(cacheKey, k -> orgMapper.selectOneById(id));
        return result.getValue();
    }

    @Override
    public boolean getTopCompanyIsAdminById(Long id) {
        List<OrgNature> sysOrgTypes = orgNatureMapper.selectListByQuery(QueryWrapper.create().eq(OrgNature::getOrgId, id));

        return sysOrgTypes.stream().anyMatch(item -> OrgNatureEnum.OPERATIONS.eq(item.getNature()));
    }

    @Override
    public OrgNature getOrgNatureByOrgId(Long id) {
        List<OrgNature> sysOrgTypes = orgNatureMapper.selectListByQuery(QueryWrapper.create().eq(OrgNature::getOrgId, id));
        return CollUtil.isNotEmpty(sysOrgTypes) ? sysOrgTypes.get(0) : null;
    }

    @Override
    public String sendPhoneCode(String oldPhone, String phone) {
        User current = getById(ContextUtil.getUserId());
        ArgumentAssert.notNull(current, "用户不存在");
        ArgumentAssert.isTrue(StrUtil.equals(current.getPhone(), oldPhone), "原手机号不一致");
        ArgumentAssert.isFalse(checkPhone(phone, null), "该手机号已经被他人使用");

        String code = RandomUtil.randomNumbers(6);
        String key = RandomUtil.randomNumbers(10);
        CacheKey cacheKey = CaptchaCacheKeyBuilder.build(key, MsgTemplateKey.Sms.PHONE_EDIT);
        cacheOps.set(cacheKey, code);

        log.info("短信验证码 cacheKey={}, code={}", cacheKey, code);

        SmsBlend smsBlend = SmsFactory.getSmsBlend("chuanglan");
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("s", code);
        String template = "【" + chuangLanConfig.getSignature() + "】您的验证码为：{$var}";
//        TODO 调用消息中心接口
        smsBlend.sendMessage(phone, template, params);
        return (key);
    }

    @Override
    public String sendEmailCode(String oldEmail, String email) {
        User current = getById(ContextUtil.getUserId());
        ArgumentAssert.notNull(current, "用户不存在");
        ArgumentAssert.isTrue(StrUtil.equals(current.getEmail(), oldEmail), "原邮箱不一致");
        ArgumentAssert.isFalse(checkEmail(email, null), "该邮箱已经被他人使用");

        String code = RandomUtil.randomNumbers(6);
        String key = RandomUtil.randomNumbers(10);
        CacheKey cacheKey = CaptchaCacheKeyBuilder.build(key, MsgTemplateKey.Email.EMAIL_EDIT);
        cacheOps.set(cacheKey, code);

        log.info("短信验证码 cacheKey={}, code={}", cacheKey, code);

        String template = "您的验证码为：" + code;

//        TODO 调用消息中心接口
        MailMessage message = MailMessage.Builder()
                .mailAddress(email) // 收件人地址
                .title("修改个人邮箱")
                .body(template)
                .build();
        mailClient.send(message);

        return (key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updatePhone(ProfilePhoneDto dto) {
        Long userId = ContextUtil.getUserId();
        ArgumentAssert.notNull(userId, "用户不存在");
        User current = getById(userId);
        ArgumentAssert.notNull(current, "用户不存在");
        ArgumentAssert.isTrue(StrUtil.equals(current.getPhone(), dto.getOldPhone()), "原手机号不一致");
        ArgumentAssert.isFalse(checkPhone(dto.getPhone(), null), "该手机号已经被他人使用");

        CacheKey cacheKey = CaptchaCacheKeyBuilder.build(dto.getKey(), MsgTemplateKey.Sms.PHONE_EDIT);
        CacheResult<String> result = cacheOps.get(cacheKey);
        ArgumentAssert.isTrue(StrUtil.equals(result.getValue(), dto.getCode()), "验证码错误");

        User user = UpdateEntity.of(User.class, userId);
        user.setPhone(dto.getPhone());
        updateById(user);

        cacheOps.del(cacheKey);
        // TODO 广播修改用户信息 & 修改session 中的user

        return userId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateEmail(ProfileEmailDto dto) {
        Long userId = ContextUtil.getUserId();
        ArgumentAssert.notNull(userId, "用户不存在");
        User current = getById(userId);
        ArgumentAssert.notNull(current, "用户不存在");
        ArgumentAssert.isTrue(StrUtil.equals(current.getEmail(), dto.getOldEmail()), "原邮箱不一致");
        ArgumentAssert.isFalse(checkPhone(dto.getEmail(), null), "该邮箱已经被他人使用");

        CacheKey cacheKey = CaptchaCacheKeyBuilder.build(dto.getKey(), MsgTemplateKey.Email.EMAIL_EDIT);
        CacheResult<String> result = cacheOps.get(cacheKey);
        ArgumentAssert.isTrue(StrUtil.equals(result.getValue(), dto.getCode()), "验证码错误");

        User user = UpdateEntity.of(User.class, userId);
        user.setEmail(dto.getEmail());
        updateById(user);

        cacheOps.del(cacheKey);
        // TODO 广播修改用户信息 & 修改session 中的user
        return userId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updatePassword(ProfilePasswordDto dto) {
        Long userId = ContextUtil.getUserId();
        ArgumentAssert.notNull(userId, "用户不存在");
        User current = getById(userId);
        ArgumentAssert.notNull(current, "用户不存在");
        String oldPasswordMd5 = SecureUtil.sha256(dto.getOldPassword() + current.getSalt());
        ArgumentAssert.isTrue(StrUtil.equals(oldPasswordMd5, current.getPassword()), "原密码不正确");
        ArgumentAssert.isTrue(StrUtil.equals(dto.getPassword(), dto.getConfirmPassword()), "密码不一致");

        User user = UpdateEntity.of(User.class, userId);
        user.setSalt(RandomUtil.randomString(20));
        user.setPassword(SecureUtil.sha256(dto.getPassword() + user.getSalt()));
        user.setPwErrorNum(0);
        user.setPwErrorLastTime(null);
        String expireTime = configFacade.getString(ConfigKey.Workbench.PASSWORD_EXPIRE_TIME, "3M");
        user.setPwExpireTime(DateUtils.conversionDateTime(LocalDateTime.now(), expireTime));

        updateById(user);
        // TODO 广播修改用户信息 & 修改session 中的user
        return userId;
    }

    private void initSsoUser(User defUser) {
        defUser.setSalt(RandomUtil.randomString(20));
        defUser.setPassword(SecureUtil.sha256(defUser.getPassword() + defUser.getSalt()));
        defUser.setPwErrorNum(0);
        defUser.setState(BooleanEnum.TRUE.getBool());
    }
}
