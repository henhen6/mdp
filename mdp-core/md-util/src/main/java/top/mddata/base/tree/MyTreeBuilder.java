package top.mddata.base.tree;

import cn.hutool.core.builder.Builder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import top.mddata.base.base.entity.TreeEntity;

import java.io.Serial;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 树构建器
 *
 * @author henhen6
 * @param <E> ID类型
 * @param <F> 实体类型
 */
public class MyTreeBuilder<E, F extends TreeEntity<E, F>> implements Builder<TreeEntity<E, F>> {
    @Serial
    private static final long serialVersionUID = 1L;
    private final Map<E, F> idTreeMap;
    private F root;
    private Integer deep;
    private boolean isBuild;
    // 新增：排序比较器（用户可自定义，null 时用默认规则）
    private Comparator<Map.Entry<E, F>> sortComparator;


    /**
     * 构造
     *
     * @param rootId 根节点ID
     */
    public MyTreeBuilder(E rootId, Supplier<F> supplier) {
        F entity = supplier.get();
        entity.setId(rootId);
        this.root = entity;
        this.idTreeMap = new LinkedHashMap<>();
        this.sortComparator = null;
    }

    /**
     * 创建Tree构建器
     *
     * @param rootId 根节点ID
     * @param supplier 节点实例构造器
     * @param <E>    ID类型
     * @return TreeBuilder
     */
    public static <E, F extends TreeEntity<E, F>> MyTreeBuilder<E, F> of(E rootId, Supplier<F> supplier) {
        return new MyTreeBuilder<>(rootId, supplier);
    }

    /**
     * 树结构的最大深度
     *
     * @param deep 深度
     * @return this
     */
    public MyTreeBuilder<E, F> setDeep(Integer deep) {
        this.deep = deep;
        return this;
    }


    /**
     * 设置ID
     *
     * @param id ID
     * @return this
     * @since 5.7.14
     */
    public MyTreeBuilder<E, F> setId(E id) {
        this.root.setId(id);
        return this;
    }

    /**
     * 设置父节点ID
     *
     * @param parentId 父节点ID
     * @return this
     * @since 5.7.14
     */
    public MyTreeBuilder<E, F> setParentId(E parentId) {
        this.root.setParentId(parentId);
        return this;
    }

    /**
     * 设置权重
     *
     * @param weight 权重
     * @return this
     * @since 5.7.14
     */
    public MyTreeBuilder<E, F> setWeight(Integer weight) {
        this.root.setWeight(weight);
        return this;
    }

    /**
     * 新增：设置节点排序规则（可选）
     * 未设置时，默认按节点 value（F 实体）升序排序（保持原有逻辑）
     *
     * @param sortComparator 排序比较器（基于 {@code Map.Entry<E, F>}，可自定义多字段排序）
     * @return this（支持链式调用）
     */
    public MyTreeBuilder<E, F> setSortComparator(Comparator<Map.Entry<E, F>> sortComparator) {
        this.sortComparator = sortComparator;
        return this;
    }

    /**
     * 增加节点列表，增加的节点是不带子节点的
     *
     * @param map 节点列表
     * @return this
     */
    public MyTreeBuilder<E, F> append(Map<E, F> map) {
        checkBuilt();

        this.idTreeMap.putAll(map);
        return this;
    }


    /**
     * 增加节点列表，增加的节点是不带子节点的
     *
     * @param trees 节点列表
     * @return this
     */
    public MyTreeBuilder<E, F> append(Iterable<F> trees) {
        checkBuilt();

        for (F tree : trees) {
            this.idTreeMap.put(tree.getId(), tree);
        }
        return this;
    }

    /**
     * 增加节点列表，增加的节点是不带子节点的
     *
     * @param list       Bean列表
     * @return this
     */
    public MyTreeBuilder<E, F> append(List<F> list) {
        checkBuilt();

        final E rootId = this.root.getId();
        final Map<E, F> map = this.idTreeMap;
        for (F node : list) {
            if (rootId != null && !rootId.getClass().equals(node.getId().getClass())) {
                throw new IllegalArgumentException("rootId type is node.getId().getClass()!");
            }
            // issue#IAUSHR 如果指定根节点存在，直接复用
            if (Objects.equals(rootId, node.getId())) {
                this.root = node;
            } else {
                //非根节点
                map.put(node.getId(), node);
            }

        }
        return append(map);
    }


    /**
     * 重置Builder，实现复用
     *
     * @return this
     */
    public MyTreeBuilder<E, F> reset() {
        this.idTreeMap.clear();
        this.root.setChildren(null);
        this.isBuild = false;
        this.sortComparator = null; // 重置排序规则
        return this;
    }

    @Override
    public F build() {
        checkBuilt();

        buildFromMap();
        cutTree();

        this.isBuild = true;
        this.idTreeMap.clear();

        return root;
    }

    /**
     * 构建树列表，没有顶层节点，例如：
     *
     * <pre>
     * -用户管理
     *  -用户管理
     *    +用户添加
     * - 部门管理
     *  -部门管理
     *    +部门添加
     * </pre>
     *
     * @return 树列表
     */
    public List<F> buildList() {
        if (isBuild) {
            // 已经构建过了
            return this.root.getChildren();
        }
        return build().getChildren();
    }

    /**
     * 开始构建
     */
    private void buildFromMap() {
        if (MapUtil.isEmpty(this.idTreeMap)) {
            return;
        }
        Map<E, F> eTreeMap = null;
        if (Objects.nonNull(this.sortComparator)) {
            // 1. 用户传递了排序规则：按规则排序，用 LinkedHashMap 保持顺序
            Map<E, F> result = new LinkedHashMap<>();
            this.idTreeMap.entrySet().stream()
                    .sorted(this.sortComparator) // 应用用户排序规则
                    .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
            eTreeMap = result;
        } else {
            eTreeMap = MapUtil.sortByValue(this.idTreeMap, false);
        }

        E parentId;
        for (F node : eTreeMap.values()) {
            if (node == null) {
                continue;
            }
            parentId = node.getParentId();
            if (ObjectUtil.equals(this.root.getId(), parentId)) {
                this.root.addChildren(node);
                continue;
            }

            final F parentNode = eTreeMap.get(parentId);
            if (parentNode != null) {
                parentNode.addChildren(node);
            }
        }
    }

    /**
     * 树剪枝
     */
    private void cutTree() {
        if (this.deep == null || this.deep < 0) {
            return;
        }
        cutTree(this.root, 0, this.deep);
    }

    /**
     * 树剪枝叶
     *
     * @param tree        节点
     * @param currentDepp 当前层级
     * @param maxDeep     最大层级
     */
    private void cutTree(F tree, int currentDepp, int maxDeep) {
        if (null == tree) {
            return;
        }
        if (currentDepp == maxDeep) {
            // 剪枝
            tree.setChildren(null);
            return;
        }

        final List<F> children = tree.getChildren();
        if (CollUtil.isNotEmpty(children)) {
            for (F child : children) {
                cutTree(child, currentDepp + 1, maxDeep);
            }
        }
    }

    /**
     * 检查是否已经构建
     */
    private void checkBuilt() {
        Assert.isFalse(isBuild, "Current tree has been built.");
    }
}
