package top.mddata.open.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @param <T> 节点类型
 * @param <I> 节点id类型
 * @author henhen6
 */
public interface TreeNode<T, I> {

    /**
     * list转换成tree
     *
     * @param list     待转换的list，即平铺的tree
     * @param parentId 当前父节点id
     * @param <I>      id类型
     * @param <T>      节点类型
     * @return 返回具有父子关系的list
     */
    static <I, T extends TreeNode<T, I>> List<T> convertTree(List<T> list, I parentId) {
        if (list == null) {
            return new ArrayList<>();
        }
        List<T> temp = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            T item = list.get(i);
            if (Objects.equals(item.takeParentId(), parentId)) {
                List<T> children = convertTree(list, item.takeId());
                item.setChildren(children);
                temp.add(item);
            }
        }
        return temp;
    }

    /**
     * 获取节点id值
     *
     * @return 返回id值
     */
    I takeId();

    /**
     * 获取父节点id
     *
     * @return 返回父节点id值
     */
    I takeParentId();

    /**
     * 设置子节点
     *
     * @param children 子节点
     */
    void setChildren(List<T> children);

}
