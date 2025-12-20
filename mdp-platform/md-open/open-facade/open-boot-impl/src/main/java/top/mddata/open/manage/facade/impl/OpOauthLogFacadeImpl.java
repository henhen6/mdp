package top.mddata.open.manage.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.open.admin.dto.OauthLogDto;
import top.mddata.open.admin.service.OauthLogService;
import top.mddata.open.manage.facade.OauthLogFacade;

/**
 *
 * @author henhen6
 * @since 2025/8/21 23:36
 */
@Service
@RequiredArgsConstructor
public class OpOauthLogFacadeImpl implements OauthLogFacade {
    private final OauthLogService oauthLogService;

    @Override
    public void save(OauthLogDto ool) {
        oauthLogService.saveDto(ool);
    }
}
