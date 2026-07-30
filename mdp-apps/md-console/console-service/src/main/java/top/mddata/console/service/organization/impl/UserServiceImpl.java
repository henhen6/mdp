package top.mddata.console.service.organization.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.mybatisflex.core.constant.SqlOperator;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.query.SqlOperators;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.base.mybatisflex.utils.BeanPageUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.DateUtils;
import top.mddata.common.cache.console.organization.UserCacheKeyBuilder;
import top.mddata.common.cache.workbench.SsoUserEmailCacheKeyBuilder;
import top.mddata.common.cache.workbench.SsoUserPhoneCacheKeyBuilder;
import top.mddata.common.cache.workbench.SsoUserUserNameCacheKeyBuilder;
import top.mddata.common.constant.ConfigKey;
import top.mddata.common.constant.EchoDictType;
import top.mddata.common.constant.EventTypeCode;
import top.mddata.common.constant.FileObjectType;
import top.mddata.common.constant.RoleCode;
import top.mddata.common.dto.IdDto;
import top.mddata.common.dto.IdsDto;
import top.mddata.common.entity.User;
import top.mddata.common.entity.UserOrgRel;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.enumeration.BooleanEnum;
import top.mddata.common.enumeration.organization.UserSourceEnum;
import top.mddata.common.enumeration.organization.UserTypeEnum;
import top.mddata.common.mapper.UserMapper;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.dto.organization.UserDto;
import top.mddata.console.dto.organization.UserResetPasswordDto;
import top.mddata.console.dto.organization.UserUpdateDto;
import top.mddata.console.dto.system.RelateFilesToBizDto;
import top.mddata.console.entity.system.DictItem;
import top.mddata.console.query.organization.UserQuery;
import top.mddata.console.service.organization.UserOrgRelService;
import top.mddata.console.service.organization.UserService;
import top.mddata.console.service.permission.RoleService;
import top.mddata.console.service.system.ConfigService;
import top.mddata.console.service.system.DictItemService;
import top.mddata.console.service.system.FileService;
import top.mddata.console.vo.organization.UserVo;
import top.mddata.open.dto.admin.EventTriggerDto;
import top.mddata.open.facade.admin.NotifyAndEventPushFacade;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 15:44:52
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends SuperServiceImpl<UserMapper, User> implements UserService {
    private final UserOrgRelService userOrgRelService;
    private final FileService fileService; // 同一个服务，直接调用 service。跨服务需要调用 facade
    private final ConfigService configService;  // 同一个服务，直接调用 service。跨服务需要调用 facade
    private final DictItemService dictItemService;
    private final SystemProperties systemProperties;
    private final UidGenerator uidGenerator;
    private final NotifyAndEventPushFacade notifyAndEventPushFacade;
    private final RoleService roleService;

    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new UserCacheKeyBuilder();
    }

    @Override
    protected User saveBefore(Object save) {
        UserDto dto = (UserDto) save;
        ArgumentAssert.isFalse(checkUsername(dto.getUsername(), null), "用户名[{}]， 重复", dto.getUsername());
        if (StrUtil.isNotEmpty(dto.getEmail())) {
            ArgumentAssert.isFalse(checkEmail(dto.getEmail(), null), "邮箱[{}]， 重复", dto.getEmail());
        }
        if (StrUtil.isNotEmpty(dto.getPhone())) {
            ArgumentAssert.isFalse(checkPhone(dto.getPhone(), null), "手机号[{}]， 重复", dto.getPhone());
        }
        User entity = BeanUtil.toBean(save, getEntityClass());
        entity.setId(uidGenerator.getUid());

        String password;
        String salt = RandomUtil.randomString(20);
        if (dto.getDefPassword() != null && dto.getDefPassword()) {
            password = SecureUtil.sha256(systemProperties.getDefPwd() + salt);
        } else {
            password = SecureUtil.sha256(dto.getPassword() + salt);
        }
        entity.setSalt(salt);
        entity.setPassword(password);
        entity.setUserType(UserTypeEnum.USER.getCode());
        String expireTime = configService.getString(ConfigKey.Workbench.PASSWORD_EXPIRE_TIME, "3M");
        entity.setPwExpireTime(DateUtils.conversionDateTime(LocalDateTime.now(), expireTime));
        entity.setUserSource(UserSourceEnum.PLATFORM.getCode());
        // 头像的对象id，可以重新生成一个唯一ID，也可以直接用当前用户ID
        entity.setAvatar(entity.getId());

        return entity;
    }

    @Override
    protected void saveAfter(Object save, User entity) {
        UserDto dto = (UserDto) save;
        List<Long> orgIdList = dto.getOrgIdList();
        saveOrg(entity, orgIdList);

        // 关联附件 注意：dto.logo 是前端传递过来的文件id， entity.logo 是在存入数据库前，设置的唯一对象id（为了节约雪花id，可以复用entity.getId(), 即可生成新的唯一id）
        fileService.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getAvatar())
                .objectType(FileObjectType.Console.USER_AVATAR)
                .build().setKeepFileIds(dto.getAvatarFileId()));

        EventTriggerDto request = new EventTriggerDto();
        request.setEventCode(EventTypeCode.Console.USER_ADD)
                .setEventContent(IdDto.builder().id(entity.getId()).build().toString())
                .setTriggerAt(LocalDateTime.now());
        notifyAndEventPushFacade.eventPush(request);

        List<CacheKey> cacheKeys = new ArrayList<>();
        if (StrUtil.isNotEmpty(dto.getUsername())) {
            cacheKeys.add(SsoUserUserNameCacheKeyBuilder.builder(dto.getUsername()));
        }
        if (StrUtil.isNotEmpty(dto.getPhone())) {
            cacheKeys.add(SsoUserPhoneCacheKeyBuilder.builder(dto.getPhone()));
        }
        if (StrUtil.isNotEmpty(dto.getEmail())) {
            cacheKeys.add(SsoUserEmailCacheKeyBuilder.builder(dto.getEmail()));
        }
        cacheOps.del(cacheKeys);
    }

    private void saveOrg(User entity, List<Long> orgIdList) {
        userOrgRelService.removeByUserIds(Collections.singletonList(entity.getId()));

        if (CollUtil.isNotEmpty(orgIdList)) {
            List<UserOrgRel> eoList = orgIdList.stream().map(orgId -> {
                UserOrgRel rel = new UserOrgRel();
                rel.setUserId(entity.getId()).setOrgId(orgId);
                return rel;
            }).toList();
            userOrgRelService.saveBatch(eoList);
        }
    }

    @Override
    protected User updateBefore(Object updateDto) {
        User sysUser = super.updateBefore(updateDto);
        ArgumentAssert.isFalse(checkUsername(sysUser.getUsername(), sysUser.getId()), "用户名[{}]， 重复", sysUser.getUsername());
        if (StrUtil.isNotEmpty(sysUser.getEmail())) {
            ArgumentAssert.isFalse(checkEmail(sysUser.getEmail(), sysUser.getId()), "邮箱[{}]， 重复", sysUser.getEmail());
        }
        if (StrUtil.isNotEmpty(sysUser.getPhone())) {
            ArgumentAssert.isFalse(checkPhone(sysUser.getPhone(), sysUser.getId()), "手机号[{}]， 重复", sysUser.getPhone());
        }
        // dto.getAvatarFileId() 是file表的id， sysUser.setAvatar 是对象id
        // 注意：前端传递的avatar是文件id，存入数据库时，需要设置为唯一的对象id（通常为了节约雪花id，可以复用entity.getId(), 也可生成新的唯一id）
        sysUser.setAvatar(sysUser.getId());

        // 防止修改了用户所属的部门信息后，登录时，切换到不存在的单位或部门。
        sysUser.setLastTopCompanyId(null);
        sysUser.setLastCompanyId(null);
        sysUser.setLastDeptId(null);
        return sysUser;
    }

    @Override
    protected void updateAfter(Object updateDto, User entity) {
        UserUpdateDto dto = (UserUpdateDto) updateDto;
        List<Long> orgIdList = dto.getOrgIdList();
        saveOrg(entity, orgIdList);

        // 关联附件 注意：dto.logo 是前端传递过来的文件id， entity.logo 是在存入数据库前，设置的唯一对象id（为了节约雪花id，可以复用entity.getId(), 也可生成新的唯一id）
        fileService.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getAvatar())
                .objectType(FileObjectType.Console.USER_AVATAR)
                .build().setKeepFileIds(dto.getAvatarFileId()));

        EventTriggerDto request = new EventTriggerDto();
        request.setEventCode(EventTypeCode.Console.USER_EDIT)
                .setEventContent(IdDto.builder().id(entity.getId()).build().toString())
                .setTriggerAt(LocalDateTime.now());
        notifyAndEventPushFacade.eventPush(request);

        List<CacheKey> cacheKeys = new ArrayList<>();
        if (StrUtil.isNotEmpty(dto.getUsername())) {
            cacheKeys.add(SsoUserUserNameCacheKeyBuilder.builder(dto.getUsername()));
        }
        if (StrUtil.isNotEmpty(dto.getPhone())) {
            cacheKeys.add(SsoUserPhoneCacheKeyBuilder.builder(dto.getPhone()));
        }
        if (StrUtil.isNotEmpty(dto.getEmail())) {
            cacheKeys.add(SsoUserEmailCacheKeyBuilder.builder(dto.getEmail()));
        }
        cacheOps.del(cacheKeys);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<UserVo> page(PageParams<UserQuery> params) {
        Page<User> page = Page.of(params.getCurrent(), params.getSize());
        User entity = BeanUtil.toBean(params.getModel(), User.class);
        SqlOperators sqlOperators = WrapperUtil.buildOperators(entity.getClass());
        sqlOperators.set(User::getSex, SqlOperator.EQUALS);
        QueryWrapper wrapper = QueryWrapper.create(entity, sqlOperators);
        wrapper.eq(User::getUserType, UserTypeEnum.USER.getCode());
        if (CollUtil.isNotEmpty(params.getModel().getOrgIdList())) {
            QueryWrapper orgWrapper = QueryWrapper.create();
            orgWrapper.select(UserOrgRel::getUserId)
                    .from(UserOrgRel.class).in(UserOrgRel::getOrgId, params.getModel().getOrgIdList());
            wrapper.in(User::getId, orgWrapper, true);
        }
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entity.getClass());
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        mapper.paginateWithRelations(page, wrapper);
        return BeanPageUtil.toBeanPage(page, UserVo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unlock(Long id) {
        User sysUser = UpdateEntity.of(User.class, id);
        sysUser.setPwErrorLastTime(null);
        sysUser.setPwErrorNum(0);
        boolean flag = updateById(sysUser);
        delCache(id);
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(UserResetPasswordDto data) {
        User user = UpdateEntity.of(User.class, data.getId());
        user.setPwErrorLastTime(null);
        user.setPwErrorNum(0);
        String password;
        String salt = RandomUtil.randomString(20);
        if (data.getDefPassword() != null && data.getDefPassword()) {
            password = SecureUtil.sha256(systemProperties.getDefPwd() + salt);
        } else {
            password = SecureUtil.sha256(data.getPassword() + salt);
        }
        user.setSalt(salt);
        user.setPassword(password);

        String expireTime = configService.getString(ConfigKey.Workbench.PASSWORD_EXPIRE_TIME, "3M");
        user.setPwExpireTime(DateUtils.conversionDateTime(LocalDateTime.now(), expireTime));

        boolean flag = updateById(user);
        delCache(data.getId());
        return flag;
    }


    @Override
    @Transactional(readOnly = true)
    public Boolean checkUsername(String username, Long id) {
        return mapper.selectCountByQuery(QueryWrapper.create().eq(User::getUsername, username).ne(User::getId, id)) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkPhone(String phone, Long id) {
        return mapper.selectCountByQuery(QueryWrapper.create().eq(User::getPhone, phone).ne(User::getId, id)) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkEmail(String email, Long id) {
        return mapper.selectCountByQuery(QueryWrapper.create().eq(User::getEmail, email).ne(User::getId, id)) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> listByRoleIds(List<Long> roleIds) {
        QueryWrapper wrapper = QueryWrapper.create().select().from(User.class).innerJoin(UserRoleRel.class).on(UserRoleRel::getUserId, User::getId)
                .where(UserRoleRel::getRoleId).in(roleIds).and(User::getState).eq(true);
        return list(wrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> listByDeptIds(List<Long> deptIds) {
        QueryWrapper wrapper = QueryWrapper.create().select().from(User.class).innerJoin(UserOrgRel.class).on(UserOrgRel::getUserId, User::getId)
                .where(UserOrgRel::getOrgId).in(deptIds).and(User::getState).eq(true);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        boolean flag = super.removeByIds(idList);
        EventTriggerDto request = new EventTriggerDto();
        request.setEventCode(EventTypeCode.Console.USER_DELETE)
                .setEventContent(IdsDto.builder().ids(idList).build().toString())
                .setTriggerAt(LocalDateTime.now());
        notifyAndEventPushFacade.eventPush(request);
        return flag;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean registerByEmail(User user) {
        ArgumentAssert.isFalse(checkEmail(user.getEmail(), null), "邮箱：{}已经存在", user.getEmail());
        user.setPassword(systemProperties.getDefPwd());
        user.setUsername(UUID.randomUUID().toString(true));
        initSsoUser(user);
        user.setName(user.getEmail());
        save(user);
        saveDefOrg(user);
        saveDefRole(user);

        EventTriggerDto request = new EventTriggerDto();
        request.setEventCode(EventTypeCode.Console.USER_ADD)
                .setEventContent(IdDto.builder().id(user.getId()).build().toString())
                .setTriggerAt(LocalDateTime.now());
        notifyAndEventPushFacade.eventPush(request);
        return true;
    }

    private void saveDefRole(User user) {
        String code = RoleCode.DEFAULT_USER;

        Map<String, DictItem> dictMap = dictItemService.getDictItemByUniqKey(EchoDictType.Workbench.REG_BIND_ROLE);

        if (dictMap.containsKey(String.valueOf(user.getUserType()))) {
            DictItem dictItem = dictMap.get(String.valueOf(user.getUserType()));
            code = dictItem != null ? dictItem.getName() : null;
        }

        roleService.joinTheRole(code, user.getId());

    }

    private void saveDefOrg(User user) {
        // 开发者 加入内置的组织
        if (UserTypeEnum.DEVELOPER.eq(user.getUserType())) {
            Long orgId = configService.getLong(ConfigKey.Console.BUILT_IN_DEVELOPER, null);
            ArgumentAssert.notNull(orgId, "请先联系管理员配置公司：内置开发者");

            UserOrgRel userOrgRel = new UserOrgRel();
            userOrgRel.setUserId(user.getId());
            userOrgRel.setOrgId(orgId);
            userOrgRelService.save(userOrgRel);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean registerByPhone(User user) {
        ArgumentAssert.isFalse(checkPhone(user.getPhone(), null), "手机号：{}已经存在", user.getPhone());
        user.setPassword(systemProperties.getDefPwd());
        user.setUsername(UUID.randomUUID().toString(true));
        initSsoUser(user);
        user.setName(user.getPhone());
        save(user);
        saveDefOrg(user);
        saveDefRole(user);

        EventTriggerDto request = new EventTriggerDto();
        request.setEventCode(EventTypeCode.Console.USER_ADD)
                .setEventContent(IdDto.builder().id(user.getId()).build().toString())
                .setTriggerAt(LocalDateTime.now());
        notifyAndEventPushFacade.eventPush(request);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean registerByUsername(User user) {
        ArgumentAssert.isFalse(checkUsername(user.getUsername(), null), "用户名：{}已经存在", user.getUsername());
        initSsoUser(user);
        user.setName(user.getUsername());
        save(user);
        saveDefOrg(user);
        saveDefRole(user);

        EventTriggerDto request = new EventTriggerDto();
        request.setEventCode(EventTypeCode.Console.USER_ADD)
                .setEventContent(IdDto.builder().id(user.getId()).build().toString())
                .setTriggerAt(LocalDateTime.now());
        notifyAndEventPushFacade.eventPush(request);
        return true;
    }

    private void initSsoUser(User defUser) {
        defUser.setSalt(RandomUtil.randomString(20));
        defUser.setPassword(SecureUtil.sha256(defUser.getPassword() + defUser.getSalt()));
        defUser.setPwErrorNum(0);
        defUser.setState(BooleanEnum.TRUE.getBool());
        defUser.setUserSource(UserSourceEnum.PLATFORM.getCode());
        String expireTime = configService.getString(ConfigKey.Workbench.PASSWORD_EXPIRE_TIME, "3M");
        defUser.setPwExpireTime(DateUtils.conversionDateTime(LocalDateTime.now(), expireTime));
    }
}
