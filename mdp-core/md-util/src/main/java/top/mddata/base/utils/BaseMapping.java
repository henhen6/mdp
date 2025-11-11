package top.mddata.base.utils;


import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Bean相互转换基类
 *
 * @param <Source> 源类
 * @param <Target> 目的类
 * @author henhen6
 * @since 2024年06月23日14:14:33
 */
public interface BaseMapping<Source, Target> {
    /**
     * Source转Target
     *
     * @param source /
     * @return /
     */
    Target toTarget(Source source);

    /**
     * Source集合转Target集合
     *
     * @param aList /
     * @return /
     */
    List<Target> toTargetList(List<Source> aList);


    /**
     * A更新B
     *
     * @param source 原始对象
     * @param target 目标对象
     */
    void copySourceProperties(Source source, @MappingTarget Target target);


    /**
     * A集合更新B集合
     *
     * @param aList /
     * @param bList /
     * @return /
     */
    void copySourceProperties(List<Source> aList, @MappingTarget List<Target> bList);


    /**
     * Target转Source
     *
     * @param target 参数
     * @return /
     */
    Source toSource(Target target);


    /**
     * B集合转A集合
     *
     * @param bList /
     * @return /
     */
    List<Source> toSource(List<Target> bList);

    /**
     * B更新A
     *
     * @param b /
     * @param a /
     */
    void copyTargetProperties(Target b, @MappingTarget Source a);


    /**
     * B集合更新A集合
     *
     * @param bList /
     * @param aList /
     */
    void copyTargetProperties(List<Target> bList, @MappingTarget List<Source> aList);
}