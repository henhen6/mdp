package top.mddata.open.facade.admin.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.open.dto.admin.OauthLogDto;
import top.mddata.open.facade.admin.OauthLogFacade;
import top.mddata.open.service.admin.OauthLogService;

/**
 *
 * @author henhen6
 * @since 2025/8/21 23:36
 */
@Service
@RequiredArgsConstructor
public class OauthLogFacadeImpl implements OauthLogFacade {
    private final OauthLogService oauthLogService;

    @Override
    public void save(OauthLogDto ool) {
        oauthLogService.saveDto(ool);
    }
}
