package top.mddata.open.admin.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.entity.OauthOpenid;
import top.mddata.open.admin.mapper.OauthOpenidMapper;
import top.mddata.open.admin.service.OauthOpenidService;

/**
 * openid 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OauthOpenidServiceImpl extends SuperServiceImpl<OauthOpenidMapper, OauthOpenid> implements OauthOpenidService {

}
