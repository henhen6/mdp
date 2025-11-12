package top.mddata.console.permission.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.mddata.base.exception.BizException;
import top.mddata.base.mvcflex.service.impl.SuperServiceImpl;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.BeanPlusUtil;
import top.mddata.base.utils.MyTreeUtil;
import top.mddata.base.utils.StrPool;
import top.mddata.common.constant.console.AdminConstant;
import top.mddata.common.enumeration.permission.MenuTypeEnum;
import top.mddata.console.permission.dto.ResourceMenuDto;
import top.mddata.console.permission.entity.ResourceMenu;
import top.mddata.console.permission.mapper.ResourceMenuMapper;
import top.mddata.console.permission.service.ResourceMenuService;
import top.mddata.console.permission.vo.ResourceMenuVo;
import top.mddata.console.permission.vo.RouterMeta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static top.mddata.common.constant.console.AdminConstant.IFRAME;
import static top.mddata.common.constant.console.AdminConstant.LAYOUT;

/**
 * 菜单 服务层实现。
 *
 * @author henhen6
 * @since 2025-11-12 16:27:16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceMenuServiceImpl extends SuperServiceImpl<ResourceMenuMapper, ResourceMenu> implements ResourceMenuService {
    private final UidGenerator uidGenerator;

    /**
     * 是否所有的子都是视图
     *
     * 若某个菜单的子集，全部都是视图，这需要标记为隐藏子集
     *
     */
    private static boolean hideChildrenInMenu(List<ResourceMenuVo> children) {
        if (CollUtil.isEmpty(children)) {
            return false;
        }

        // hidden： true - 视图   false - 菜单  null - 菜单
        return children.stream().allMatch(item -> {
            RouterMeta meta = item.getMeta();
            return meta != null && meta.getHideInMenu() != null && meta.getHideInMenu();
        });
    }

    /**
     * 1. 管理员拥有全部菜单；普通用户根据角色进行查询；
     * 2. 构造树结构
     * 3. 构造vueRouter数据
     * @return
     */
    @Override
    public List<ResourceMenuVo> findAllMenu(Long applicationId) {
        // 1 查询所有的菜单
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(ResourceMenu::getApplicationId, applicationId).eq(ResourceMenu::getState, true);
        queryWrapper.orderBy(ResourceMenu::getWeight, true);
        List<ResourceMenu> menuList = list(queryWrapper);
        List<ResourceMenuVo> resultList = BeanUtil.copyToList(menuList, ResourceMenuVo.class);

        if (CollUtil.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        // 2 转换树结构， hutool的工具类性能好一些
        List<ResourceMenuVo> menuTreeList = MyTreeUtil.buildTreeEntity(resultList, ResourceMenuVo::new);

        // 3 构造前端需要的vueRouter数据，此方法可以考虑在前端做
        forEachTree(menuTreeList, 1, null);
        return menuTreeList;
    }

    private void forEachTree(List<ResourceMenuVo> tree, int level, ResourceMenuVo parent) {

        for (ResourceMenuVo node : tree) {

            if (MenuTypeEnum.INNER_HREF.eq(node.getMenuType())) {
                node.setComponent(IFRAME);
            } else if (MenuTypeEnum.OUTER_HREF.eq(node.getMenuType())) {
                node.setComponent(IFRAME);
            }

            RouterMeta meta = node.getMeta();

            if (meta != null) {
                meta.setTitle(StrUtil.isEmpty(meta.getTitle()) ? node.getName() : meta.getTitle());
            } else {
                meta = new RouterMeta();
                meta.setTitle(node.getName());
            }

            meta.setComponent(node.getComponent());
            if (meta.getHideInMenu() != null && meta.getHideInMenu() && StrUtil.isEmpty(meta.getActivePath()) && parent != null) {
                meta.setActivePath(Convert.toStr(parent.getPath()));
            }

            // 是否所有的子都是隐藏菜单
            boolean hideChildrenInMenu = hideChildrenInMenu(node.getChildren());
            meta.setHideChildrenInMenu(hideChildrenInMenu);

            if (CollUtil.isNotEmpty(node.getChildren())) {
                String component = node.getComponent();
                if (StrUtil.isEmpty(component)) {
                    node.setComponent(LAYOUT);
                }

                // 若[某菜单]下所有子集都是隐藏菜单，就将[某菜单]数据克隆到他的子集的第一个菜单，并且点击[某菜单]时，重定向到子集第一个菜单
                if (hideChildrenInMenu) {
                    String path = node.getPath();
                    CharSequence name = node.getName();

                    ResourceMenuVo cloneItem = new ResourceMenuVo();
                    BeanUtil.copyProperties(node, cloneItem);
                    RouterMeta firstMeta = BeanPlusUtil.toBean(meta, RouterMeta.class);
                    firstMeta.setActivePath(path).setHideInMenu(true);
                    cloneItem.setChildren(new ArrayList<>());
                    cloneItem.setMeta(firstMeta);
                    cloneItem.setComponent(component);
                    cloneItem.setPath("");
                    cloneItem.setName(name + "Child");

                    List<ResourceMenuVo> childrenList = node.getChildren();
                    childrenList.add(0, cloneItem);

                }
            }

            node.setMeta(meta);

            if (CollUtil.isNotEmpty(node.getChildren())) {
                forEachTree(node.getChildren(), level + 1, node);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkCode(Long applicationId, String code, Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(ResourceMenu::getApplicationId, applicationId)
                .eq(ResourceMenu::getCode, code)
                .ne(ResourceMenu::getId, id);
        return count(queryWrapper) > 0;
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean checkPath(Long applicationId, String path, Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(ResourceMenu::getApplicationId, applicationId)
                .eq(ResourceMenu::getPath, path)
                .ne(ResourceMenu::getId, id);
        return count(queryWrapper) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean checkName(Long applicationId, String name, Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(ResourceMenu::getApplicationId, applicationId)
                .eq(ResourceMenu::getName, name)
                .ne(ResourceMenu::getId, id);
        return count(queryWrapper) > 0;
    }

    @Override
    protected ResourceMenu saveBefore(Object save) {
        ResourceMenuDto data = (ResourceMenuDto) save;
        ArgumentAssert.isFalse(checkCode(data.getApplicationId(), data.getCode(), null), "编码重复：{}", data.getCode());
        ArgumentAssert.isFalse(checkName(data.getApplicationId(), data.getName(), null), "菜单名称重复：{}", data.getName());
        ArgumentAssert.isFalse(checkPath(data.getApplicationId(), data.getPath(), null), "路由地址重复：{}", data.getPath());

        ResourceMenu parent = validateAndFill(data);

        ResourceMenu sysMenu = super.saveBefore(data);
        sysMenu.setId(uidGenerator.getUid());
        fill(sysMenu, parent);

        return sysMenu;
    }

    private ResourceMenu validateAndFill(ResourceMenuDto data) {
        RouterMeta meta = data.getMeta();
        if (meta == null) {
            meta = new RouterMeta();
        }
        if (StrUtil.isEmpty(meta.getTitle())) {
            meta.setTitle(data.getName());
        }
        data.setMeta(meta);

        ResourceMenu parent = null;
        if (data.getParentId() != null) {
            parent = getById(data.getParentId());
            ArgumentAssert.notNull(parent, "上级菜单不能为空");
        }

        // 目录只能挂在目录下
        if (StrUtil.equals(data.getMenuType(), MenuTypeEnum.DIR.getCode())) {
            if (parent != null) {
                ArgumentAssert.isTrue(MenuTypeEnum.DIR.eq(parent.getMenuType()), "【{}】的上级节点只能是【{}】", MenuTypeEnum.DIR.getDesc(), MenuTypeEnum.DIR.getDesc());
            }
            if (StrUtil.isEmpty(data.getComponent())) {
                data.setComponent(LAYOUT);
            }
        } else if (StrUtil.equals(data.getMenuType(), MenuTypeEnum.MENU.getCode())) {
            if (parent != null) {
                ArgumentAssert.isTrue(StrUtil.equalsAny(parent.getMenuType(), MenuTypeEnum.MENU.getCode(), MenuTypeEnum.DIR.getCode()),
                        "【{}】的上级节点只能是【{}、{}】", MenuTypeEnum.MENU.getDesc(), MenuTypeEnum.DIR.getDesc(), MenuTypeEnum.MENU.getDesc());
            }
            ArgumentAssert.notEmpty(data.getComponent(), "请填写【页面路径】");
        } else if (StrUtil.equals(data.getMenuType(), MenuTypeEnum.INNER_HREF.getCode())) {
            if (parent != null) {
                ArgumentAssert.isTrue(MenuTypeEnum.DIR.eq(parent.getMenuType()), "【{}】只能挂载在【{}】下级", MenuTypeEnum.INNER_HREF.getDesc(), MenuTypeEnum.DIR.getDesc());
            }
            ArgumentAssert.notEmpty(meta.getIframeSrc(), "请填写【链接地址】");
            data.setComponent(AdminConstant.IFRAME);
        } else if (StrUtil.equals(data.getMenuType(), MenuTypeEnum.OUTER_HREF.getCode())) {
            if (parent != null) {
                ArgumentAssert.isTrue(MenuTypeEnum.DIR.eq(parent.getMenuType()), "【{}】只能挂载在【{}】下级", MenuTypeEnum.OUTER_HREF.getDesc(), MenuTypeEnum.DIR.getDesc());
            }
            ArgumentAssert.notEmpty(meta.getLink(), "请填写【链接地址】");
            data.setComponent(AdminConstant.IFRAME);
        }
        return parent;
    }

    @Override
    protected void saveAfter(Object save, ResourceMenu entity) {
        ResourceMenuDto dto = (ResourceMenuDto) save;
        super.saveAfter(save, entity);

        // 操作其他数据
    }


    @Override
    protected ResourceMenu updateBefore(Object update) {
        ResourceMenuDto data = (ResourceMenuDto) update;
        ArgumentAssert.isFalse(checkCode(data.getApplicationId(), data.getCode(), data.getId()), "编码重复：{}", data.getCode());
        ArgumentAssert.isFalse(checkName(data.getApplicationId(), data.getName(), data.getId()), "菜单名称重复：{}", data.getName());
        ArgumentAssert.isFalse(checkPath(data.getApplicationId(), data.getPath(), data.getId()), "路由地址重复：{}", data.getPath());

        if (data.getMeta() == null) {
            data.setMeta(new RouterMeta());
        }

        ResourceMenu parent = validateAndFill(data);

        ResourceMenu sysMenu = super.updateBefore(data);
        fill(sysMenu, parent);

        return sysMenu;
    }

    @Override
    protected void updateAfter(Object updateDto, ResourceMenu entity) {
        super.updateAfter(updateDto, entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceMenuVo getDefMenuByParentId(Long applicationId, Long id) {
        ResourceMenuVo sysMenu = new ResourceMenuVo();
        sysMenu.setApplicationId(applicationId);
        sysMenu.setState(true);
        sysMenu.setMeta(new RouterMeta().setKeepAlive(true));
        sysMenu.setParentId(id);

        if (id == null) {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.select(QueryMethods.max(ResourceMenu::getWeight))
                    .eq(ResourceMenu::getApplicationId, applicationId)
                    .isNull(ResourceMenu::getParentId);
            Integer weight = getObjAs(queryWrapper, Integer.class);
            weight = weight == null ? 0 : weight;
            sysMenu.setWeight(weight + 10);
            sysMenu.setMenuType(MenuTypeEnum.DIR.getCode());
            return sysMenu;
        }

        ResourceMenu parent = getById(id);
        if (parent == null) {
            throw new BizException("父节点不存在");
        }

        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select(QueryMethods.max(ResourceMenu::getWeight))
                .eq(ResourceMenu::getApplicationId, applicationId)
                .eq(ResourceMenu::getParentId, id);
        Integer weight = getObjAs(queryWrapper, Integer.class);
        weight = weight == null ? 0 : weight;
        sysMenu.setWeight(weight + 1);
        sysMenu.setMenuType(MenuTypeEnum.MENU.getCode());
        sysMenu.setCode(parent.getCode() + StrPool.COLON);
        return sysMenu;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceMenu> findAllChildrenByParentId(Long parentId) {
        ArgumentAssert.notNull(parentId, "parentId 不能为空");
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.like(ResourceMenu::getTreePath, MyTreeUtil.buildTreePath(parentId)).orderBy(ResourceMenu::getWeight);
        return list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void move(Long sourceId, Long targetId) {
        ArgumentAssert.notNull(sourceId, "当前菜单不存在");

        ResourceMenu current = getByIdCache(sourceId);
        ArgumentAssert.notNull(current, "当前菜单不存在");
        List<ResourceMenu> childrenList = findAllChildrenByParentId(sourceId);


        // 是否跟节点移动到根节点 （跟节点可能为null)
        boolean isTopMoveTop = MyTreeUtil.isRoot(current.getParentId()) && MyTreeUtil.isRoot(targetId);
        // 是否自己移动到自己下
        boolean isSelfMoveSelf = current.getParentId() != null && current.getParentId().equals(targetId);
        if (isTopMoveTop || isSelfMoveSelf) {
            log.info("没有改变 id={}, targetId={}", sourceId, targetId);
            return;
        }

        ResourceMenu parent = null;
        if (targetId != null) {
            ArgumentAssert.isFalse(sourceId.equals(targetId), "不能成为自己的子节点");
            boolean flag = childrenList.stream().anyMatch(item -> item.getId().equals(targetId));
            ArgumentAssert.isFalse(flag, "不能移动到自己的子节点");
            parent = getByIdCache(targetId);
            ArgumentAssert.notNull(parent, "需要移动到的父资源不存在");
        }

        if (CollUtil.isNotEmpty(childrenList)) {
            ResourceMenu tree = MyTreeUtil.buildSingleTreeEntity(childrenList, current.getId(), ResourceMenu::new);
            fill(tree, parent);
            if (CollUtil.isNotEmpty(tree.getChildren())) {
                recursiveFill(tree.getChildren(), tree);
            }

            super.updateBatch(childrenList);
        }

        // 只修改了 父ID、path等字段，清理资源缓存，无需清理 资源API的缓存
        List<Long> allIdList = childrenList.stream().map(ResourceMenu::getId).collect(Collectors.toList());
        allIdList.add(current.getId());
        delCache(allIdList);
    }


    private void recursiveFill(List<ResourceMenu> tree, ResourceMenu parent) {
        for (ResourceMenu node : tree) {
            fill(node, parent);

            if (CollUtil.isNotEmpty(node.getChildren())) {
                recursiveFill(node.getChildren(), node);
            }
        }
    }

    private void fill(ResourceMenu resource, ResourceMenu parent) {
        if (parent == null) {
            resource.setParentId(MyTreeUtil.DEF_PARENT_ID);
            resource.setTreePath(MyTreeUtil.buildTreePath(resource.getId()));
        } else {
            resource.setParentId(parent.getId());
            resource.setTreePath(MyTreeUtil.buildTreePath(parent.getTreePath(), resource.getId()));
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        if (CollUtil.isEmpty(idList)) {
            return false;
        }
        List<ResourceMenu> sysMenus = listByIds(idList);
        if (CollUtil.isEmpty(sysMenus)) {
            return false;
        }
//        删除他的子集
        sysMenus.forEach(sysMenu -> remove(QueryWrapper.create().likeLeft(ResourceMenu::getTreePath, sysMenu.getTreePath())));
        return super.removeByIds(idList);
    }

}
