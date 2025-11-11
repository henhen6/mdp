package top.mddata.common.constant;


/**
 * 默认值
 *
 * @author henhen6
 * @date 2025年08月07日19:21:13
 */
public interface DefValConstants {
    /**
     * 默认的排序
     */
    Integer SORT_VALUE = 0;

    /** 防止字典空值导致的 缓存击穿问题 */
    String DICT_NULL_VAL_KEY = "-999999999";
}
