package top.mddata.console.organization.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.base.mybatisflex.utils.BeanPageUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.DateUtils;
import top.mddata.common.cache.console.organization.UserCacheKeyBuilder;
import top.mddata.common.constant.ConfigKey;
import top.mddata.common.constant.FileObjectType;
import top.mddata.common.entity.User;
import top.mddata.common.entity.UserOrgRel;
import top.mddata.common.entity.UserRoleRel;
import top.mddata.common.enumeration.organization.UserSourceEnum;
import top.mddata.common.enumeration.organization.UserTypeEnum;
import top.mddata.common.mapper.UserMapper;
import top.mddata.common.properties.SystemProperties;
import top.mddata.console.organization.dto.UserDto;
import top.mddata.console.organization.dto.UserResetPasswordDto;
import top.mddata.console.organization.dto.UserUpdateDto;
import top.mddata.console.organization.query.UserQuery;
import top.mddata.console.organization.service.UserOrgRelService;
import top.mddata.console.organization.service.UserService;
import top.mddata.console.organization.vo.UserVo;
import top.mddata.console.system.dto.RelateFilesToBizDto;
import top.mddata.console.system.service.ConfigService;
import top.mddata.console.system.service.FileService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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
    private final SystemProperties systemProperties;
    private final UidGenerator uidGenerator;

    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new UserCacheKeyBuilder();
    }

    @Override
    protected User saveBefore(Object save) {
        UserDto dto = (UserDto) save;
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
        entity.setAvatar(entity.getId());

        return entity;
    }

    @Override
    protected void saveAfter(Object save, User entity) {
        UserDto dto = (UserDto) save;
        ArgumentAssert.isFalse(checkUsername(dto.getUsername(), null), "用户名[{}]， 重复", dto.getUsername());
        if (StrUtil.isNotEmpty(dto.getEmail())) {
            ArgumentAssert.isFalse(checkEmail(dto.getEmail(), null), "邮箱[{}]， 重复", dto.getEmail());
        }
        if (StrUtil.isNotEmpty(dto.getPhone())) {
            ArgumentAssert.isFalse(checkPhone(dto.getPhone(), null), "手机号[{}]， 重复", dto.getPhone());
        }

        List<Long> orgIdList = dto.getOrgIdList();
        saveOrg(entity, orgIdList);

        // 关联附件 注意：dto.logo 是前端传递过来的文件id， entity.logo 是在存入数据库前，设置的唯一对象id（为了节约雪花id，可以复用entity.getId(), 即可生成新的唯一id）
        fileService.relateFilesToBiz(RelateFilesToBizDto.builder()
                .objectId(entity.getAvatar())
                .objectType(FileObjectType.Console.USER_AVATAR)
                .build().setKeepFileIds(dto.getAvatar()));
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
                .build().setKeepFileIds(dto.getAvatar()));
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
        User sysUser = UpdateEntity.of(User.class, data.getId());
        sysUser.setPwErrorLastTime(null);
        sysUser.setPwErrorNum(0);
        String password;
        String salt = RandomUtil.randomString(20);
        if (data.getDefPassword() != null && data.getDefPassword()) {
            password = SecureUtil.sha256(systemProperties.getDefPwd() + salt);
        } else {
            password = SecureUtil.sha256(data.getPassword() + salt);
        }
        sysUser.setSalt(salt);
        sysUser.setPassword(password);

        String expireTime = configService.getString(ConfigKey.Workbench.PASSWORD_EXPIRE_TIME, "3M");
        sysUser.setPwExpireTime(DateUtils.conversionDateTime(LocalDateTime.now(), expireTime));

        boolean flag = updateById(sysUser);
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
}
