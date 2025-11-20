package top.mddata.open.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.entity.OauthUnionid;
import top.mddata.open.mapper.OauthUnionidMapper;
import top.mddata.open.service.OauthUnionidService;

/**
 * unionid 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:33:43
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OauthUnionidServiceImpl extends SuperServiceImpl<OauthUnionidMapper, OauthUnionid> implements OauthUnionidService {

}
