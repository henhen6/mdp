package top.mddata.open.service.doc.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.open.dto.admin.HelpDocDto;
import top.mddata.open.entity.admin.HelpDoc;
import top.mddata.open.mapper.admin.HelpDocMapper;
import top.mddata.open.service.doc.HelpDocService;

import java.util.ArrayList;
import java.util.List;

/**
 * 帮助文档 服务层实现。
 *
 * @author henhen6
 * @since 2026-01-02 10:11:40
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HelpDocServiceImpl extends SuperServiceImpl<HelpDocMapper, HelpDoc> implements HelpDocService {
    private final UidGenerator uidGenerator;

    @Override
    protected HelpDoc saveBefore(Object save) {
        HelpDoc entity = BeanUtil.toBean(save, HelpDoc.class);
        entity.setId(null);

        HelpDoc parent = null;
        if (entity.getParentId() != null) {
            parent = getById(entity.getParentId());
            ArgumentAssert.notNull(parent, "上级节点不能为空");
        }
        entity.setId(uidGenerator.getUid());
        fill(entity, parent);
        return entity;
    }

    @Override
    protected HelpDoc updateBefore(Object update) {
        HelpDocDto data = (HelpDocDto) update;
        HelpDoc entity = UpdateEntity.of(HelpDoc.class);
        BeanUtil.copyProperties(update, entity);

        HelpDoc parent = null;
        if (data.getParentId() != null) {
            parent = getById(data.getParentId());
            ArgumentAssert.notNull(parent, "上级节点不能为空");
        }

        fill(entity, parent);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(Long sourceId, Long targetId) {
        ArgumentAssert.notNull(sourceId, "当前节点不存在");

        HelpDoc current = getByIdCache(sourceId);
        ArgumentAssert.notNull(current, "当前节点不存在");
        List<HelpDoc> childrenList = findAllChildrenByParentId(sourceId);

        // 是否跟节点移动到根节点 （跟节点可能为null)
        boolean isTopMoveTop = MyTreeUtil.isRoot(current.getParentId()) && MyTreeUtil.isRoot(targetId);
        // 是否自己移动到自己下
        boolean isSelfMoveSelf = current.getParentId() != null && current.getParentId().equals(targetId);
        if (isTopMoveTop || isSelfMoveSelf) {
            log.info("没有改变 id={}, targetId={}", sourceId, targetId);
            return;
        }

        HelpDoc parent = null;
        if (targetId != null) {
            ArgumentAssert.isFalse(sourceId.equals(targetId), "不能成为自己的子节点");
            boolean flag = childrenList.stream().anyMatch(item -> item.getId().equals(targetId));
            ArgumentAssert.isFalse(flag, "不能移动到自己的子节点");
            parent = getByIdCache(targetId);
            ArgumentAssert.notNull(parent, "需要移动到的父资源不存在");
        }

        if (CollUtil.isNotEmpty(childrenList)) {
            HelpDoc tree = MyTreeUtil.buildSingleTreeEntity(childrenList, current.getId(), HelpDoc::new);
            fill(tree, parent);
            if (CollUtil.isNotEmpty(tree.getChildren())) {
                recursiveFill(tree.getChildren(), tree);
            }

            List<HelpDoc> list = new ArrayList<>();
            childrenList.forEach(child -> {
                HelpDoc entity = UpdateEntity.of(HelpDoc.class, child.getId());
                entity.setParentId(child.getParentId());
                entity.setTreePath(child.getTreePath());
                entity.setId(child.getId());
                list.add(entity);
            });
            super.updateBatch(list);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<HelpDoc> findAllChildrenByParentId(Long parentId) {
        ArgumentAssert.notNull(parentId, "parentId 不能为空");
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.like(HelpDoc::getTreePath, MyTreeUtil.buildTreePath(parentId)).orderBy(HelpDoc::getWeight);
        return list(queryWrapper);
    }

    private void recursiveFill(List<HelpDoc> tree, HelpDoc parent) {
        for (HelpDoc node : tree) {
            fill(node, parent);

            if (CollUtil.isNotEmpty(node.getChildren())) {
                recursiveFill(node.getChildren(), node);
            }
        }
    }

    private void fill(HelpDoc item, HelpDoc parent) {
        if (parent == null) {
            item.setParentId(MyTreeUtil.DEF_PARENT_ID);
            item.setTreePath(MyTreeUtil.buildTreePath(item.getId()));
        } else {
            item.setParentId(parent.getId());
            item.setTreePath(MyTreeUtil.buildTreePath(parent.getTreePath(), item.getId()));
        }
    }
}
