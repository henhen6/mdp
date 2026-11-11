package top.mddata.workbench.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import top.mddata.base.exception.BizException;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.entity.UserSocialAuth;
import top.mddata.common.mapper.UserSocialAuthMapper;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.UserSocialAuthService;
import top.mddata.workbench.service.social.model.SocialIdentity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户第三方身份绑定 服务层实现。
 *
 * @author tangyh
 */
@Service
@Slf4j
public class UserSocialAuthServiceImpl extends SuperServiceImpl<UserSocialAuthMapper, UserSocialAuth>
        implements UserSocialAuthService {

    @Override
    public UserSocialAuth bind(Long userId, SocialIdentity identity) {
        findByIdentity(identity).ifPresent(item -> {
            throw new BizException("该第三方账号已绑定其他用户");
        });
        findByUserAndProvider(userId, identity.getProvider()).ifPresent(item -> {
            throw new BizException("当前账号已绑定该平台，请先解绑");
        });
        UserSocialAuth entity = new UserSocialAuth();
        entity.setUserId(userId);
        entity.setProvider(identity.getProvider().name());
        entity.setClientId(identity.getClientId());
        entity.setOpenId(identity.getOpenId());
        entity.setUnionId(identity.getUnionId());
        entity.setNickname(identity.getNickname());
        entity.setBoundAt(LocalDateTime.now());
        try {
            save(entity);
            return entity;
        } catch (DuplicateKeyException ex) {
            throw new BizException("第三方绑定关系已发生变化，请重新操作", ex);
        }
    }

    @Override
    public Optional<UserSocialAuth> findByIdentity(SocialIdentity identity) {
        return Optional.ofNullable(mapper.selectOneByQuery(QueryWrapper.create()
                .eq(UserSocialAuth::getProvider, identity.getProvider().name())
                .eq(UserSocialAuth::getClientId, identity.getClientId())
                .eq(UserSocialAuth::getOpenId, identity.getOpenId())));
    }

    @Override
    public Optional<UserSocialAuth> findByUserAndProvider(Long userId, SocialProviderEnum provider) {
        return Optional.ofNullable(mapper.selectOneByQuery(QueryWrapper.create()
                .eq(UserSocialAuth::getUserId, userId)
                .eq(UserSocialAuth::getProvider, provider.name())));
    }

    @Override
    public List<UserSocialAuth> listByUser(Long userId) {
        return mapper.selectListByQuery(QueryWrapper.create()
                .eq(UserSocialAuth::getUserId, userId));
    }

    @Override
    public long countByUser(Long userId) {
        return mapper.selectCountByQuery(QueryWrapper.create()
                .eq(UserSocialAuth::getUserId, userId));
    }

    @Override
    public void unbind(Long id, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        int affected = mapper.logicalDeleteByUser(id, userId, userId,
                System.currentTimeMillis(), now);
        ArgumentAssert.isTrue(affected == 1, () -> new BizException("绑定关系已发生变化，请刷新后重试"));
    }

    @Override
    public void transfer(Long id, Long sourceUserId, Long targetUserId, Long operatorId) {
        try {
            int affected = mapper.transferUser(id, sourceUserId, targetUserId,
                    operatorId, LocalDateTime.now());
            ArgumentAssert.isTrue(affected == 1, () -> new BizException("绑定关系已发生变化，请刷新后重试"));
        } catch (DuplicateKeyException ex) {
            throw new BizException("目标账号已绑定该平台，或第三方身份发生冲突", ex);
        }
    }
}
