package top.mddata.open.service.oauth.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.open.entity.admin.OauthUnionid;
import top.mddata.open.mapper.admin.OauthUnionidMapper;
import top.mddata.open.service.oauth.OauthUnionidService;
import top.mddata.open.vo.admin.OauthUnionidVo;

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OauthUnionidVo getBySubjectIdAndUserId(Long subjectId, Long userId) {
        OauthUnionid oauthUnionid = getOne(QueryWrapper.create().eq(OauthUnionid::getSubjectId, subjectId).eq(OauthUnionid::getUserId, userId));
        if (oauthUnionid == null) {
            oauthUnionid = new OauthUnionid();
            oauthUnionid.setSubjectId(subjectId);        // 主体id
            oauthUnionid.setUserId(userId);        // 所属用户id
            oauthUnionid.setUnionid(IdUtil.fastSimpleUUID());        // 开放id
            save(oauthUnionid);
        }
        return BeanUtil.toBean(oauthUnionid, OauthUnionidVo.class);
    }
}
