package top.mddata.open.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.admin.entity.OauthOpenid;
import top.mddata.open.admin.mapper.OauthOpenidMapper;
import top.mddata.open.admin.service.AppService;
import top.mddata.open.admin.service.OauthOpenidService;
import top.mddata.open.admin.vo.AppVo;
import top.mddata.open.admin.vo.OauthOpenidVo;

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
    private final AppService appService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OauthOpenidVo getByAppKeyAndUserId(String appKey, Long userId) {
        AppVo opApplicationVo = appService.getAppByAppKey(appKey);

        OauthOpenid oauthOpenid = getOne(QueryWrapper.create().eq(OauthOpenid::getAppId, opApplicationVo.getId()).eq(OauthOpenid::getUserId, userId));
        if (oauthOpenid == null) {
            oauthOpenid = new OauthOpenid();
            oauthOpenid.setUserId(userId);
            oauthOpenid.setOpenid(IdUtil.fastSimpleUUID());
            oauthOpenid.setAppId(opApplicationVo.getId());
            save(oauthOpenid);
        }
        return BeanUtil.toBean(oauthOpenid, OauthOpenidVo.class);
    }
}
