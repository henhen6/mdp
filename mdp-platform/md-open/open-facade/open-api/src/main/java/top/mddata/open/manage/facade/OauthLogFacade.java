package top.mddata.open.manage.facade;

import top.mddata.open.admin.dto.OauthLogDto;

/**
 * 授权日志
 *
 * @author henhen6
 * @since 2025/8/21 23:33
 */
public interface OauthLogFacade {
    /**
     * 保存授权记录
     *
     * @param ool 记录
     */
    void save(OauthLogDto ool);
}
