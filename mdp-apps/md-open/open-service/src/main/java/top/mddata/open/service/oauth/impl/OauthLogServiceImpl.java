package top.mddata.open.service.oauth.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.entity.admin.OauthLog;
import top.mddata.open.mapper.admin.OauthLogMapper;
import top.mddata.open.service.oauth.OauthLogService;

/**
 * 授权记录 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OauthLogServiceImpl extends SuperServiceImpl<OauthLogMapper, OauthLog> implements OauthLogService {

}
