package top.mddata.workbench.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.common.entity.UserSocialAuth;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.service.social.model.SocialIdentity;

import java.util.List;
import java.util.Optional;

/**
 * 用户第三方身份绑定 服务层。
 *
 * @author tangyh
 */
public interface UserSocialAuthService extends SuperService<UserSocialAuth> {

    /**
     * 绑定第三方身份
     *
     * @param userId   用户ID
     * @param identity 第三方身份
     * @return 绑定记录
     */
    UserSocialAuth bind(Long userId, SocialIdentity identity);

    /**
     * 按第三方身份查询绑定记录
     *
     * @param identity 第三方身份
     * @return 绑定记录
     */
    Optional<UserSocialAuth> findByIdentity(SocialIdentity identity);

    /**
     * 按用户和平台查询绑定记录
     *
     * @param userId   用户ID
     * @param provider 第三方平台
     * @return 绑定记录
     */
    Optional<UserSocialAuth> findByUserAndProvider(Long userId, SocialProviderEnum provider);

    /**
     * 列出用户的全部绑定记录
     *
     * @param userId 用户ID
     * @return 绑定记录列表
     */
    List<UserSocialAuth> listByUser(Long userId);

    /**
     * 统计用户的绑定数量
     *
     * @param userId 用户ID
     * @return 绑定数量
     */
    long countByUser(Long userId);

    /**
     * 解绑第三方身份
     *
     * @param id     绑定记录ID
     * @param userId 当前用户ID
     */
    void unbind(Long id, Long userId);

    /**
     * 将绑定从源用户转移到目标用户
     *
     * @param id           绑定记录ID
     * @param sourceUserId 源用户ID
     * @param targetUserId 目标用户ID
     * @param operatorId   操作人ID
     */
    void transfer(Long id, Long sourceUserId, Long targetUserId, Long operatorId);
}
