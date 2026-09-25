package top.mddata.console.service.organization.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baidu.fsg.uid.UidGenerator;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.model.cache.CacheKeyBuilder;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.CollHelper;
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.common.cache.console.organization.OrgCacheKeyBuilder;
import top.mddata.common.constant.BuiltInOrgId;
import top.mddata.common.constant.EventTypeCode;
import top.mddata.common.dto.IdDto;
import top.mddata.common.dto.IdsDto;
import top.mddata.common.entity.Org;
import top.mddata.common.enumeration.organization.OrgNatureEnum;
import top.mddata.common.enumeration.organization.OrgTypeEnum;
import top.mddata.common.mapper.OrgMapper;
import top.mddata.console.dto.organization.OrgDto;
import top.mddata.console.service.organization.OrgService;
import top.mddata.console.service.organization.SystemProtectService;
import top.mddata.console.service.organization.UserOrgRelService;
import top.mddata.open.dto.admin.EventTriggerDto;
import top.mddata.open.facade.admin.NotifyAndEventPushFacade;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 组织 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 15:49:10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrgServiceImpl extends SuperServiceImpl<OrgMapper, Org> implements OrgService {
    private final UidGenerator uidGenerator;
    private final UserOrgRelService userOrgRelService;
    private final NotifyAndEventPushFacade notifyAndEventPushFacade;
    private final SystemProtectService systemProtectService;

    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new OrgCacheKeyBuilder();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(Long sourceId, Long targetId) {
        ArgumentAssert.notNull(sourceId, "当前节点不存在");
        // 内置组织不允许移动（置于 notNull 之后，避免 List.of(null) 抛出无意义的 NPE）
        systemProtectService.checkOrgNotBuiltIn(List.of(sourceId), "移动组织");

        Org current = getByIdCache(sourceId);
        ArgumentAssert.notNull(current, "当前节点不存在");
        List<Org> childrenList = findAllChildrenByParentId(sourceId);

        // 是否跟节点移动到根节点 （跟节点可能为null)
        boolean isTopMoveTop = MyTreeUtil.isRoot(current.getParentId()) && MyTreeUtil.isRoot(targetId);
        // 是否自己移动到自己下
        boolean isSelfMoveSelf = current.getParentId() != null && current.getParentId().equals(targetId);
        if (isTopMoveTop || isSelfMoveSelf) {
            log.info("没有改变 id={}, targetId={}", sourceId, targetId);
            return;
        }

        Org parent = null;
        if (targetId != null) {
            ArgumentAssert.isFalse(sourceId.equals(targetId), "不能成为自己的子节点");
            boolean flag = childrenList.stream().anyMatch(item -> item.getId().equals(targetId));
            ArgumentAssert.isFalse(flag, "不能移动到自己的子节点");
            parent = getByIdCache(targetId);
            ArgumentAssert.notNull(parent, "需要移动到的父资源不存在");
            ArgumentAssert.isFalse(OrgTypeEnum.COMPANY.eq(current.getOrgType()) && OrgTypeEnum.DEPT.eq(parent.getOrgType()),
                    "{}不能移动到{}下级", OrgTypeEnum.COMPANY.getDesc(), OrgTypeEnum.DEPT.getDesc());
        }

        if (CollUtil.isNotEmpty(childrenList)) {
            Org tree = MyTreeUtil.buildSingleTreeEntity(childrenList, current.getId(), Org::new);
            fill(tree, parent);
            if (CollUtil.isNotEmpty(tree.getChildren())) {
                recursiveFill(tree.getChildren(), tree);
            }

            List<Org> list = new ArrayList<>();
            childrenList.forEach(child -> {
                Org org = UpdateEntity.of(Org.class, child.getId());
                org.setParentId(child.getParentId());
                org.setTreePath(child.getTreePath());
                org.setId(child.getId());
                list.add(org);
            });
            super.updateBatch(list);
        }

        // 只修改了 父ID、path等字段，清理缓存
        List<Long> allIdList = childrenList.stream().map(Org::getId).collect(Collectors.toList());
        allIdList.add(current.getId());
        delCache(allIdList);
    }


    private void recursiveFill(List<Org> tree, Org parent) {
        for (Org node : tree) {
            fill(node, parent);

            if (CollUtil.isNotEmpty(node.getChildren())) {
                recursiveFill(node.getChildren(), node);
            }
        }
    }

    private void fill(Org item, Org parent) {
        if (parent == null) {
            item.setParentId(MyTreeUtil.DEF_PARENT_ID);
            item.setTreePath(MyTreeUtil.buildTreePath(item.getId()));
        } else {
            item.setParentId(parent.getId());
            item.setTreePath(MyTreeUtil.buildTreePath(parent.getTreePath(), item.getId()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Org> findAllChildrenByParentId(Long parentId) {
        ArgumentAssert.notNull(parentId, "parentId 不能为空");
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.like(Org::getTreePath, MyTreeUtil.buildTreePath(parentId)).orderBy(Org::getWeight);
        return list(queryWrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        Set<Serializable> param = new HashSet<>();
        ids.forEach(item -> {
            if (item instanceof Collection<?> tempItem) {
                List<Long> list = Arrays.stream(Convert.toLongArray(tempItem)).toList();
                param.addAll(list);
            } else {
                param.add(Convert.toLong(item));
            }
        });

        List<Org> list = findByIds(param, null);

        return CollHelper.uniqueIndex(list.stream().filter(Objects::nonNull).toList(), Org::getId, Org::getName);
    }

    @Override
    protected Org saveBefore(Object save) {
        OrgDto data = (OrgDto) save;
        Org org = super.saveBefore(data);

        Org parent = null;
        if (data.getParentId() != null) {
            parent = getById(data.getParentId());
            ArgumentAssert.notNull(parent, "上级节点不能为空");

            if (OrgTypeEnum.COMPANY.eq(org.getOrgType())) {
                ArgumentAssert.isFalse(OrgTypeEnum.DEPT.eq(parent.getOrgType()), "{}不能挂在{}下", OrgTypeEnum.COMPANY.getDesc(), OrgTypeEnum.DEPT.getDesc());
            }
        }

        org.setId(uidGenerator.getUid());
        fill(org, parent);
        // 部门冗余同步所属公司性质；根节点默认总公司性质
        org.setNature(parent != null ? parent.getNature() : OrgNatureEnum.HEAD_COMPANY.getCode());

        return org;
    }

    @Override
    protected void saveAfter(Object save, Org entity) {
        EventTriggerDto request = new EventTriggerDto();
        request.setEventCode(EventTypeCode.Console.ORG_ADD)
                .setEventContent(IdDto.builder().id(entity.getId()).build().toString())
                .setTriggerAt(LocalDateTime.now());
        notifyAndEventPushFacade.eventPush(request);
    }

    @Override
    protected Org updateBefore(Object updateDto) {

        OrgDto data = (OrgDto) updateDto;
        Org org = super.updateBefore(data);

        // 内置组织的层级结构固定：3 个根公司必须保持根节点，默认部门必须挂在总公司下
        if (SystemProtectService.isBuiltInOrg(org.getId())) {
            // 内置组织承担运营中心/开发者平台/注册落点等系统职责，类型和状态被改会破坏这些系统约定
            Org old = getById(org.getId());
            ArgumentAssert.equals(old.getOrgType(), org.getOrgType(),
                    "修改组织失败：内置组织[{}]的类型不允许修改", org.getName());
            ArgumentAssert.equals(old.getState(), org.getState(),
                    "修改组织失败：内置组织[{}]的状态不允许修改", org.getName());

            if (Objects.equals(org.getId(), BuiltInOrgId.DEFAULT_DEPT)) {
                ArgumentAssert.equals(BuiltInOrgId.HEAD_COMPANY, org.getParentId(),
                        "修改组织失败：内置组织[{}]的上级组织必须保持为总公司", org.getName());
            } else {
                ArgumentAssert.isTrue(MyTreeUtil.isRoot(org.getParentId()),
                        "修改组织失败：内置组织[{}]必须保持根节点", org.getName());
            }
        }

        Org parent = null;
        if (data.getParentId() != null) {
            parent = getById(data.getParentId());
            ArgumentAssert.notNull(parent, "上级节点不能为空");

            if (OrgTypeEnum.COMPANY.eq(org.getOrgType())) {
                ArgumentAssert.isFalse(OrgTypeEnum.DEPT.eq(parent.getOrgType()), "{}不能挂在{}下", OrgTypeEnum.COMPANY.getDesc(), OrgTypeEnum.DEPT.getDesc());
            }
        }

        fill(org, parent);
        // 根节点的组织性质不允许经 console 编辑变更（内置根公司性质固定，防止误操作抹掉运营/开发者性质）
        if (parent != null) {
            org.setNature(parent.getNature());
        }
        return org;
    }

    @Override
    protected void updateAfter(Object update, Org entity) {

        EventTriggerDto request = new EventTriggerDto();
        request.setEventCode(EventTypeCode.Console.ORG_EDIT)
                .setEventContent(IdDto.builder().id(entity.getId()).build().toString())
                .setTriggerAt(LocalDateTime.now());
        notifyAndEventPushFacade.eventPush(request);

        log.info("org edit over");
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        if (idList.isEmpty()) {
            return false;
        }

        systemProtectService.checkOrgNotBuiltIn(idList.stream()
                .map(id -> Long.valueOf(String.valueOf(id))).toList(), "删除组织");

        boolean flag = super.removeByIds(idList);

//        删除 人员-组织
        userOrgRelService.removeByOrgIds(idList);
//        删除 角色-组织
//        baseEmployeeOrgRelManager.deleteByOrg(idList);


        EventTriggerDto request = new EventTriggerDto();
        request.setEventCode(EventTypeCode.Console.ORG_DELETE)
                .setEventContent(IdsDto.builder().ids(idList).build().toString())
                .setTriggerAt(LocalDateTime.now());
        notifyAndEventPushFacade.eventPush(request);
        return flag;
    }
}
