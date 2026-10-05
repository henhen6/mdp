package top.mddata.console.service.permission.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.mybatisflex.core.mask.MaskManager;
import com.mybatisflex.core.update.UpdateWrapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.fieldperm.engine.BuiltinMasker;
import top.mddata.base.fieldperm.model.FieldRule;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.common.cache.console.permission.ResourceFieldUriMenuCacheKeyBuilder;
import top.mddata.console.entity.permission.ResourceField;
import top.mddata.console.entity.permission.RoleFieldRel;
import top.mddata.console.mapper.permission.ResourceFieldMapper;
import top.mddata.console.service.permission.ResourceFieldService;
import top.mddata.console.service.permission.RoleFieldRelService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 字段权限 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceFieldServiceImpl extends SuperServiceImpl<ResourceFieldMapper, ResourceField> implements ResourceFieldService {

    /** 实体类字段名格式：字母开头，字母/数字/下划线 */
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

    private final RoleFieldRelService roleFieldRelService;

    @Override
    protected ResourceField saveBefore(Object save) {
        ResourceField entity = super.saveBefore(save);
        validate(entity);
        return entity;
    }

    @Override
    protected ResourceField updateBefore(Object update) {
        ResourceField entity = super.updateBefore(update);
        // 按请求体提交字段更新语义下，entity 只含提交的字段，校验需以"DB值叠加本次修改"的合并结果为准
        ResourceField merged = mergeWithDb(entity);
        validate(merged);
        // ruleType/maskRule 任一提交时，按合并结果回写 maskRule，保证两者联动一致（如改为隐藏时清空遗留规则）
        Map<String, Object> updates = ((UpdateWrapper) entity).getUpdates();
        if (updates.containsKey("ruleType") || updates.containsKey("maskRule")) {
            entity.setMaskRule(FieldRule.RULE_TYPE_MASK == merged.getRuleType() ? merged.getMaskRule() : null);
        }
        return entity;
    }

    @Override
    protected void saveAfter(Object save, ResourceField entity) {
        invalidate(List.of(entity.getId()));
    }

    @Override
    protected void updateAfter(Object update, ResourceField entity) {
        invalidate(List.of(entity.getId()));
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        List<Long> fieldIds = idList.stream().map(Convert::toLong).toList();
        boolean flag = super.removeByIds(idList);
        invalidate(fieldIds);
        return flag;
    }

    /** DB 现值叠加本次提交的修改，得到合并后的完整字段规则用于校验 */
    private ResourceField mergeWithDb(ResourceField entity) {
        ResourceField db = getById(entity.getId());
        ArgumentAssert.notNull(db, "字段规则不存在或已被删除");
        ResourceField merged = BeanUtil.copyProperties(db, ResourceField.class);
        ((UpdateWrapper) entity).getUpdates().forEach((property, value) ->
                BeanUtil.setFieldValue(merged, String.valueOf(property), value));
        return merged;
    }

    /** 录入校验（fail fast） */
    private void validate(ResourceField entity) {
        ArgumentAssert.notNull(entity.getMenuId(), "请选择所属菜单");
        ArgumentAssert.notEmpty(entity.getProperty(), "请填写实体类字段");
        ArgumentAssert.isTrue(PROPERTY_PATTERN.matcher(entity.getProperty()).matches(),
                "实体类字段【{}】格式不正确：需字母开头，仅含字母、数字、下划线", entity.getProperty());

        Integer ruleType = entity.getRuleType() == null ? FieldRule.RULE_TYPE_HIDE : entity.getRuleType();
        ArgumentAssert.isTrue(FieldRule.RULE_TYPE_HIDE == ruleType || FieldRule.RULE_TYPE_MASK == ruleType,
                "处理动作【{}】不正确：仅支持 10-隐藏、20-脱敏", ruleType);
        entity.setRuleType(ruleType);

        if (FieldRule.RULE_TYPE_MASK == ruleType) {
            ArgumentAssert.notEmpty(entity.getMaskRule(), "处理动作为脱敏时，请填写脱敏规则");
            boolean registered = MaskManager.getProcessorMap().containsKey(entity.getMaskRule())
                    || BuiltinMasker.registeredNames().contains(entity.getMaskRule());
            ArgumentAssert.isTrue(registered, "脱敏规则【{}】未注册", entity.getMaskRule());
        } else {
            entity.setMaskRule(null);
        }

        // 同菜单下 property 唯一（DB 唯一键兜底，这里给出友好提示）
        long count = count(QueryWrapper.create()
                .where(ResourceField::getMenuId).eq(entity.getMenuId())
                .and(ResourceField::getProperty).eq(entity.getProperty())
                .and(ResourceField::getId).ne(entity.getId()));
        ArgumentAssert.isTrue(count == 0, "该菜单下已存在字段【{}】的规则", entity.getProperty());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByMenuIds(Collection<? extends Serializable> menuIdList) {
        if (CollUtil.isEmpty(menuIdList)) {
            return false;
        }
        List<Long> fieldIds = mapper.selectListByQuery(QueryWrapper.create()
                        .select(ResourceField::getId)
                        .where(ResourceField::getMenuId).in(menuIdList))
                .stream().map(ResourceField::getId).toList();
        if (fieldIds.isEmpty()) {
            return false;
        }
        // 先删字段规则与角色字段关系，再统一失效缓存（uri-menu 预解析 + 相关用户受限集）
        mapper.deleteByQuery(QueryWrapper.create()
                .where(ResourceField::getMenuId).in(menuIdList));
        roleFieldRelService.remove(QueryWrapper.create()
                .where(RoleFieldRel::getFieldId).in(fieldIds));
        invalidate(fieldIds);
        return true;
    }

    /**
     * 配置变更失效：URI→菜单预解析映射全量失效；
     * 用户受限集缓存仅失效"授权了这些字段"的角色下的用户
     */
    private void invalidate(List<Long> fieldIds) {
        cacheOps.del(ResourceFieldUriMenuCacheKeyBuilder.build());
        if (CollUtil.isNotEmpty(fieldIds)) {
            roleFieldRelService.invalidateUserFieldPermCacheByFieldIds(fieldIds);
        }
    }
}
