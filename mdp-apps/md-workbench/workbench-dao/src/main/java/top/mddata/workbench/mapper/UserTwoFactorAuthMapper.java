package top.mddata.workbench.mapper;

import org.springframework.stereotype.Repository;
import top.mddata.base.mvcflex.mapper.SuperMapper;
import top.mddata.workbench.entity.UserTwoFactorAuth;

/**
 * 双因素认证开通记录 Mapper
 *
 * @author tangyh
 * @since 2026-11-16
 */
@Repository
public interface UserTwoFactorAuthMapper extends SuperMapper<UserTwoFactorAuth> {
}
