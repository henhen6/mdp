package top.mddata.base.db.typehandler;

import top.mddata.base.utils.StrPool;
import cn.hutool.core.util.StrUtil;

public class SqlUtils {

    /**
     * mybatis plus like查询转换
     */
    public static String keywordConvert(String value) {
        if (StrUtil.isBlank(value)) {
            return StrPool.EMPTY;
        }
        value = value.replaceAll(StrPool.PERCENT, "\\\\%");
        value = value.replaceAll(StrPool.UNDERSCORE, "\\\\_");
        return value;
    }

    public static Object keywordConvert(Object value) {
        if (value instanceof String str) {
            return keywordConvert(str);
        }
        return value;
    }

    /**
     * 拼接like条件
     *
     * @param value   值
     * @param sqlType 拼接类型
     * @return 拼接后的值
     */
    public static String like(Object value, SqlLike sqlType) {
        return concatLike(keywordConvert(String.valueOf(value)), sqlType);
    }


    /**
     * 用%连接like
     *
     * @param str 原字符串
     * @return like 的值
     */
    public static String concatLike(Object str, SqlLike type) {
        return switch (type) {
            case LEFT -> StrPool.PERCENT + str;
            case RIGHT -> str + StrPool.PERCENT;
            default -> StrPool.PERCENT + str + StrPool.PERCENT;
        };
    }

    /**
     * 拼接like 模糊条件
     *
     * @param value 值
     * @return 拼接后的值
     */
    public static String fullLike(String value) {
        return like(value, SqlLike.DEFAULT);
    }

}
