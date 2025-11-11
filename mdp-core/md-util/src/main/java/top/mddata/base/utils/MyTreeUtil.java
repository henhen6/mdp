package top.mddata.base.utils;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.lang.tree.parser.NodeParser;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import top.mddata.base.base.entity.TreeEntity;
import top.mddata.base.tree.MyTreeBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * list列表 转换成tree列表
 * Created by Ace on 2017/6/12.
 *
 * @author henhen6
 */
public final class MyTreeUtil {
    /**
     * 默认的树节点 分隔符
     */
    public static final String TREE_SPLIT = StrPool.SLASH;

    /**
     * 默认的父id
     */
    public static final Long DEF_PARENT_ID = null;
    /**
     * 根节点路径的索引
     */
    private static final int TOP_LEVEL = 1;

    private MyTreeUtil() {
    }

    /**
     * 构建树节点的 层级路径
     *
     * @param parentTreePath 父节点的路径
     * @param id             当前节点的id
     * @return 当前节点的路径
     */
    public static String buildTreePath(String parentTreePath, Long id) {
        return parentTreePath + id + TREE_SPLIT;
    }

    /**
     * 构建树节点的 层级路径
     *
     * @param id 当前节点的id
     * @return 当前节点的路径
     */
    public static String buildTreePath(Long id) {
        return TREE_SPLIT + id + TREE_SPLIT;
    }

    /**
     * 判断id是否为根节点
     *
     * @param parentId 当前节点的父id
     * @return 是否根节点
     */
    public static boolean isRoot(Long parentId) {
        return ObjectUtil.equals(parentId, DEF_PARENT_ID);
    }


    /**
     * 根据树节点的路径，获取树结构的根节点id
     *
     * @param treePath 路径
     * @return 根节点id
     */
    public static Long getTopNodeId(String treePath) {
        String[] pathIds = StrUtil.splitToArray(treePath, TREE_SPLIT);
        if (ArrayUtil.isNotEmpty(pathIds)) {
            return Convert.toLong(pathIds[TOP_LEVEL]);
        }
        return null;
    }

    /**
     * 构建 根节点存储null，节点ID类型为Long 的树
     *
     * @param <T>  转换的实体 为数据源里的对象类型
     * @param list 源数据集合; 必须继承TreeNode<Long>
     * @return List
     */
    public static <T extends TreeNode<Long>> List<Tree<Long>> build(List<T> list) {
        return build(list, TreeNodeConfig.DEFAULT_CONFIG);
    }

    /**
     * 构建 根节点存储null，节点ID类型为Long 的树
     *
     * @param <T>            转换的实体 为数据源里的对象类型
     * @param list           源数据集合; 必须继承TreeNode<Long>
     * @param treeNodeConfig 配置
     * @return List
     */
    public static <T extends TreeNode<Long>> List<Tree<Long>> build(List<T> list, TreeNodeConfig treeNodeConfig) {
        return build(list, DEF_PARENT_ID, treeNodeConfig);
    }

    /**
     * 构建  根节点存储 <code>rootId</code>，节点ID类型为Long 的树
     *
     * @param <T>    转换的实体 为数据源里的对象类型
     * @param <E>    ID类型
     * @param list   源数据集合; 必须继承TreeNode<E>
     * @param rootId 最顶层父id值 一般为 0 或 null 之类
     * @return List
     */
    public static <T extends TreeNode<E>, E> List<Tree<E>> build(List<T> list, E rootId) {
        return build(list, rootId, TreeNodeConfig.DEFAULT_CONFIG);
    }

    /**
     * 构建  根节点存储 <code>rootId</code>，节点ID类型为Long 的树
     *
     * @param <T>            转换的实体 为数据源里的对象类型
     * @param <E>            ID类型
     * @param list           源数据集合; 必须继承TreeNode<E>
     * @param rootId         最顶层父id值 一般为 0 或 null 之类
     * @param treeNodeConfig 配置
     * @return List
     */
    public static <T extends TreeNode<E>, E> List<Tree<E>> build(List<T> list, E rootId, TreeNodeConfig treeNodeConfig) {
        return TreeUtil.build(list, rootId, treeNodeConfig, new MyNodeParser<>());
    }


    /**
     * 构建  根节点存储 <code>rootId</code>，节点ID类型为Long 的树
     *
     * @param <T>        转换的实体 为数据源里的对象类型
     * @param list       源数据集合; 必须继承TreeNode<E>
     * @param nodeParser 解析器
     * @return List
     */
    public static <T extends TreeNode<Long>> List<Tree<Long>> build(List<T> list, NodeParser<T, Long> nodeParser) {
        return TreeUtil.build(list, DEF_PARENT_ID, TreeNodeConfig.DEFAULT_CONFIG, nodeParser);
    }

    /**
     * 树构建
     *
     * @param <F>     转换的实体 为数据源里的对象类型
     * @param <E>     ID类型
     * @param list    源数据集合
     * @param nodeNew 树节点对象构造器
     * @return List
     */
    public static <E, F extends TreeEntity<E, F>> List<F> buildTreeEntity(List<F> list, Supplier<F> nodeNew) {
        return buildTreeEntity(list, (E) DEF_PARENT_ID, nodeNew);
    }

    /**
     * 树构建
     *
     * @param <F>     转换的实体 为数据源里的对象类型
     * @param <E>     ID类型
     * @param list    源数据集合
     * @param deep    最大深度
     * @param nodeNew 树节点对象构造器
     * @return List
     */
    public static <E, F extends TreeEntity<E, F>> List<F> buildTreeEntity(List<F> list, Integer deep, Supplier<F> nodeNew) {
        return buildTreeEntity(list, (E) DEF_PARENT_ID, deep, nodeNew);
    }


    /**
     * 树构建
     *
     * @param <F>     转换的实体 为数据源里的对象类型
     * @param <E>     ID类型
     * @param list    源数据集合
     * @param rootId  最顶层父id值 一般为 0 之类
     * @param nodeNew 树节点对象构造器
     * @return List
     */
    public static <E, F extends TreeEntity<E, F>> List<F> buildTreeEntity(List<F> list, E rootId, Supplier<F> nodeNew) {
        return buildSingleTreeEntity(list, rootId, nodeNew).getChildren();
    }

    /**
     * 树构建
     *
     * @param <F>     转换的实体 为数据源里的对象类型
     * @param <E>     ID类型
     * @param list    源数据集合
     * @param rootId  最顶层父id值 一般为 0 之类
     * @param deep    最大深度
     * @param nodeNew 树节点对象构造器
     * @return List
     */
    public static <E, F extends TreeEntity<E, F>> List<F> buildTreeEntity(List<F> list, E rootId, Integer deep, Supplier<F> nodeNew) {
        return buildSingleTreeEntity(list, rootId, deep, nodeNew).getChildren();
    }

    /**
     * 构建单root节点树<br>
     * 它会生成一个以指定ID为ID的空的节点，然后逐级增加子节点。
     *
     * @param <F>     转换的实体 为数据源里的对象类型
     * @param <E>     ID类型
     * @param list    源数据集合
     * @param rootId  最顶层父id值 一般为 0 之类
     * @param nodeNew 树节点对象构造器
     * @return {@link TreeEntity}
     * @since 5.7.2
     */
    public static <E, F extends TreeEntity<E, F>> F buildSingleTreeEntity(List<F> list, E rootId, Supplier<F> nodeNew) {
        return MyTreeBuilder.of(rootId, nodeNew).append(list).build();
    }

    /**
     * 构建单root节点树<br>
     * 它会生成一个以指定ID为ID的空的节点，然后逐级增加子节点。
     *
     * @param <F>     转换的实体 为数据源里的对象类型
     * @param <E>     ID类型
     * @param list    源数据集合
     * @param rootId  最顶层父id值 一般为 0 之类
     * @param nodeNew 树节点对象构造器
     * @param deep    最大深度
     * @return {@link TreeEntity}
     * @since 5.7.2
     */
    public static <E, F extends TreeEntity<E, F>> F buildSingleTreeEntity(List<F> list, E rootId, Integer deep, Supplier<F> nodeNew) {
        return MyTreeBuilder.of(rootId, nodeNew).setDeep(deep).append(list).build();
    }


    public static class MyNodeParser<T extends TreeNode<E>, E> implements NodeParser<T, E> {
        @Override
        public void parse(T treeNode, Tree<E> tree) {
            tree.setId(treeNode.getId());
            tree.setParentId(treeNode.getParentId());
            tree.setWeight(treeNode.getWeight());
            tree.setName(treeNode.getName());

            //扩展字段
            final Map<String, Object> extra = treeNode.getExtra();
            if (MapUtil.isNotEmpty(extra)) {
                extra.forEach(tree::putExtra);
            }
        }
    }
}
