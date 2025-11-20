package top.mddata.open.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.open.admin.entity.GroupScopeRel;
import top.mddata.open.admin.entity.OauthScope;
import top.mddata.open.admin.enumeration.ScopeLevelEnum;
import top.mddata.open.admin.mapper.OauthScopeMapper;
import top.mddata.open.admin.service.GroupScopeRelService;
import top.mddata.open.admin.service.OauthScopeService;
import top.mddata.open.admin.vo.OauthScopeVo;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * oauth2权限 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-20 16:31:25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OauthScopeServiceImpl extends SuperServiceImpl<OauthScopeMapper, OauthScope> implements OauthScopeService {
    private final GroupScopeRelService groupScopeRelService;

    @Override
    @Transactional(readOnly = true)
    public boolean check(String code, Long id) {
        return super.exists(QueryWrapper.create().eq(OauthScope::getCode, code).ne(OauthScope::getId, id));
    }

    @Override
    public long getMaxWeight() {
        Long maxWeight = super.getOneAs(QueryWrapper.create().select(QueryMethods.max(OauthScope::getWeight)).from(OauthScope.class), Long.class);
        return maxWeight == null ? 1 : maxWeight + 1;
    }

    @Override
    protected OauthScope saveBefore(Object save) {
        OauthScope entity = BeanUtil.toBean(save, getEntityClass());
        entity.setId(null);
        ArgumentAssert.isFalse(check(entity.getCode(), null), "权限编码重复");
        if (ScopeLevelEnum.SPECIAL.eq(entity.getLevel())) {
            ArgumentAssert.notEmpty(entity.getApplyPrompt(), "申请提示语不能为空");
        }
        if (entity.getWeight() == null) {
            entity.setWeight(getMaxWeight());
        }
        return entity;
    }

    @Override
    protected OauthScope updateBefore(Object updateDto) {
        // 这个方法可以让你在调用updateById时，将手动设置过的所有字段，就修改到数据库。 没有手动调用过set的字段，不会更新
        OauthScope entity = UpdateEntity.of(getEntityClass());
        BeanUtil.copyProperties(updateDto, entity);
        ArgumentAssert.isFalse(check(entity.getCode(), entity.getId()), "权限编码重复");
        if (ScopeLevelEnum.SPECIAL.eq(entity.getLevel())) {
            ArgumentAssert.notEmpty(entity.getApplyPrompt(), "申请提示语不能为空");
        }
        return entity;
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        boolean flag = super.removeByIds(idList);
        groupScopeRelService.remove(QueryWrapper.create().in(GroupScopeRel::getScopeId, idList));
        return flag;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OauthScopeVo> getScopeListByCode(List<String> scopes) {
        return listAs(QueryWrapper.create().in(OauthScope::getCode, scopes), OauthScopeVo.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OauthScopeVo> listByAppId(Long appId) {
        List<OauthScope> opScopes = getMapper().selectByAppId(appId, ScopeLevelEnum.OPEN.getCode());
        return BeanUtil.copyToList(opScopes, OauthScopeVo.class);
    }
}
