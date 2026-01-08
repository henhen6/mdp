package top.mddata.api.oepn.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import top.mddata.api.oepn.UserOpenService;
import top.mddata.api.oepn.dto.UserBatchSaveDto;
import top.mddata.api.oepn.dto.UserSaveDto;
import top.mddata.api.oepn.dto.UserUpdateDto;
import top.mddata.api.oepn.query.UserQuery;
import top.mddata.api.oepn.vo.UserListVo;
import top.mddata.api.oepn.vo.UserVo;
import top.mddata.base.model.cache.CacheKey;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.mvcflex.utils.WrapperUtil;
import top.mddata.base.mybatisflex.utils.BeanPageUtil;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.CollHelper;
import top.mddata.common.cache.console.organization.UserCacheKeyBuilder;
import top.mddata.common.cache.workbench.SsoUserEmailCacheKeyBuilder;
import top.mddata.common.cache.workbench.SsoUserPhoneCacheKeyBuilder;
import top.mddata.common.cache.workbench.SsoUserUserNameCacheKeyBuilder;
import top.mddata.common.entity.User;
import top.mddata.common.enumeration.organization.UserTypeEnum;
import top.mddata.common.mapper.UserMapper;
import top.mddata.common.properties.SystemProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户数据接口
 * @author henhen
 * @since 2026/1/7 11:13
 */
@DubboService
@Slf4j
@Service
@RequiredArgsConstructor
public class UserOpenServiceImpl extends SuperServiceImpl<UserMapper, User> implements UserOpenService {
    private final SystemProperties systemProperties;

    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new UserCacheKeyBuilder();
    }

    @Override
    public UserListVo batchSave(UserBatchSaveDto list) {
        List<UserSaveDto> dto = list.getList();
        ArgumentAssert.notEmpty(dto, "用户信息不能为空");
        ArgumentAssert.isFalse(dto.size() > 500, "每次保存的用户数量不能超过500条");

        List<String> usernameList2 = dto.stream().map(UserSaveDto::getUsername).filter(StrUtil::isNotEmpty).distinct().toList();
        ArgumentAssert.isTrue(dto.size() == usernameList2.size(), "用户名存在重复数据");
        List<String> phoneList1 = dto.stream().map(UserSaveDto::getPhone).filter(StrUtil::isNotEmpty).toList();
        List<String> phoneList2 = dto.stream().map(UserSaveDto::getPhone).filter(StrUtil::isNotEmpty).distinct().toList();
        ArgumentAssert.isTrue(phoneList1.size() == phoneList2.size(), "手机号存在重复数据");
        List<String> emailList1 = dto.stream().map(UserSaveDto::getEmail).filter(StrUtil::isNotEmpty).toList();
        List<String> emailList2 = dto.stream().map(UserSaveDto::getEmail).filter(StrUtil::isNotEmpty).distinct().toList();
        ArgumentAssert.isTrue(emailList1.size() == emailList2.size(), "邮箱存在重复数据");

        List<User> existUsernameList = mapper.selectListByQuery(QueryWrapper.create().in(User::getUsername, usernameList2));
        Map<String, User> userMap = CollHelper.buildMap(existUsernameList, User::getUsername, item -> item);

        // 用户名重复的数据
        List<UserVo> existList = new ArrayList<>();
        List<User> saveList = new ArrayList<>();
        List<User> updateList = new ArrayList<>();
        for (UserSaveDto userDto : dto) {

            if (userMap.containsKey(userDto.getUsername())) {
                User oldUser = userMap.get(userDto.getUsername());
                if (StrUtil.equals(oldUser.getUserSource(), userDto.getUserSource())) {
                    // 更新
                    User sysUser = UpdateEntity.of(User.class, oldUser.getId());
                    BeanUtil.copyProperties(userDto, sysUser);
                    String salt = RandomUtil.randomString(20);
                    String password = SecureUtil.sha256(systemProperties.getDefPwd() + salt);
                    sysUser.setSalt(salt);
                    sysUser.setPassword(password);
                    sysUser.setUserType(UserTypeEnum.USER.getCode());
                    sysUser.setState(true);
                    updateList.add(sysUser);
                } else {
                    // 用户名相同，来源不同的数据不处理。
                    existList.add(BeanUtil.toBean(userDto, UserVo.class));
                }
            } else {
                // 新增
                User sysUser = BeanUtil.toBean(userDto, User.class);
                sysUser.setId(null);

                String salt = RandomUtil.randomString(20);
                String password = SecureUtil.sha256(systemProperties.getDefPwd() + salt);
                sysUser.setSalt(salt);
                sysUser.setPassword(password);
                sysUser.setUserType(UserTypeEnum.USER.getCode());
                sysUser.setState(true);
                saveList.add(sysUser);
            }
        }

        saveBatch(saveList);
        updateBatch(updateList);
        UserListVo listVo = new UserListVo();
        listVo.setExistList(existList).setSaveList(BeanUtil.copyToList(saveList, UserVo.class)).setUpdateList(BeanUtil.copyToList(updateList, UserVo.class));

        // 清理缓存
        List<CacheKey> idKeys = updateList.stream().map(User::getId).distinct().map(UserCacheKeyBuilder::builder).toList();
        List<CacheKey> usernameKeys = dto.stream().map(UserSaveDto::getUsername).filter(StrUtil::isNotEmpty).distinct().map(SsoUserUserNameCacheKeyBuilder::builder).toList();
        List<CacheKey> phoneKeys = dto.stream().map(UserSaveDto::getPhone).filter(StrUtil::isNotEmpty).distinct().map(SsoUserPhoneCacheKeyBuilder::builder).toList();
        List<CacheKey> emailKeys = dto.stream().map(UserSaveDto::getEmail).filter(StrUtil::isNotEmpty).distinct().map(SsoUserEmailCacheKeyBuilder::builder).toList();
        cacheOps.del(CollHelper.addAll(idKeys, usernameKeys, phoneKeys, emailKeys));

        return listVo;
    }

    @Override
    public UserVo updateById(UserUpdateDto dto) {
        ArgumentAssert.notNull(dto, "用户信息不能为空");
        long existUsername = mapper.selectCountByQuery(QueryWrapper.create().eq(User::getUsername, dto.getUsername()).ne(User::getId, dto.getId()));
        ArgumentAssert.isTrue(existUsername <= 0, "用户名[{}]存在重复数据", dto.getUsername());
        long existPhone = mapper.selectCountByQuery(QueryWrapper.create().eq(User::getPhone, dto.getPhone()).ne(User::getId, dto.getId()));
        ArgumentAssert.isTrue(existPhone <= 0, "手机[{}]存在重复数据", dto.getPhone());
        long existEmail = mapper.selectCountByQuery(QueryWrapper.create().eq(User::getEmail, dto.getEmail()).ne(User::getId, dto.getId()));
        ArgumentAssert.isTrue(existEmail <= 0, "邮箱[{}]存在重复数据", dto.getEmail());

        User sysUser = BeanUtil.toBean(dto, User.class);
        mapper.update(sysUser);

        delCache(sysUser);
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

        return BeanUtil.toBean(sysUser, UserVo.class);
    }

    @Override
    public UserVo getById(Long id) {
        User sysUser = mapper.selectOneById(id);
        return BeanUtil.toBean(sysUser, UserVo.class);
    }

    @Override
    public Page<UserVo> page(PageParams<UserQuery> params) {
        Page<User> page = Page.of(params.getCurrent(), params.getSize());
        User entity = BeanUtil.toBean(params.getModel(), User.class);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entity.getClass()));
        WrapperUtil.buildWrapperByOrder(wrapper, params, entity.getClass());
        mapper.paginate(page, wrapper);
        return BeanPageUtil.toBeanPage(page, UserVo.class);
    }
}
