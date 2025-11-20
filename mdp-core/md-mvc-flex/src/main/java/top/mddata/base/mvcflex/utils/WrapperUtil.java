package top.mddata.base.mvcflex.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.core.constant.SqlOperator;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.query.SqlOperators;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import com.mybatisflex.core.util.ClassUtil;
import org.apache.ibatis.util.MapUtil;
import top.mddata.base.base.ExtraParams;
import top.mddata.base.exception.BizException;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.DateUtils;
import top.mddata.base.utils.StrPool;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WrapperUtil {

    /**
     * 分隔符
     */
    public static final String UNDERSCORE = StrPool.UNDERSCORE;
    /**
     * 开始时间
     */
    public static final String ST = "st";
    /**
     * 结束时间
     */
    public static final String ED = "ed";
    /**
     * 开始时间
     */
    public static final String START = "start";
    /**
     * 结束时间
     */
    public static final String END = "end";
    /**
     * 等于
     */
    public static final String EQ = "eq";
    /**
     * 不等于
     */
    public static final String NE = "ne";
    /**
     * 大于等于
     */
    public static final String GE = "ge";
    /**
     * 大于
     */
    public static final String GT = "gt";
    /**
     * 小于
     */
    public static final String LT = "lt";
    /**
     * 小于等于
     */
    public static final String LE = "le";
    /**
     * 模糊
     */
    public static final String LIKE = "like";
    /**
     * 左模糊
     */
    public static final String LIKE_LEFT = "likeLeft";
    /**
     * 右模糊
     */
    public static final String LIKE_RIGHT = "likeRight";
    /**
     * 范围内
     */
    public static final String IN = "in";

    /** 实体类对应的操作符 */
    private static final Map<Class<?>, SqlOperators> SQL_OPERATORS_MAP = new ConcurrentHashMap<>();

    /**
     * 根据实体类，自动构建查询的条件，  字符串类型的用like ，其他类型用默认值
     * @param entityClass 实体类
     * @return sql操作符
     */
    public static SqlOperators buildOperators(Class<?> entityClass) {
        return new SqlOperators(MapUtil.computeIfAbsent(SQL_OPERATORS_MAP, entityClass, aClass -> {
            SqlOperators sqlOperators = new SqlOperators();
            List<Field> allFields = ClassUtil.getAllFields(entityClass);
            TableInfo tableInfo = TableInfoFactory.ofEntityClass(ClassUtil.getUsefulClass(entityClass));
            allFields.forEach(field -> {
                if (field.getType() == String.class) {
                    Column column = field.getAnnotation(Column.class);
                    if (column != null && column.ignore()) {
                        return;
                    }
                    sqlOperators.set(tableInfo.getColumnByProperty(field.getName()), SqlOperator.LIKE);
                }
            });
            return sqlOperators;
        }));
    }


    /**
     * 根据扩展参数构建Wrapper
     *
     * @param wrapper 查询条件
     * @param params 扩展参数
     * @param entity 实体类
     */
    public static void buildWrapperByExtra(QueryWrapper wrapper, ExtraParams params, Class<?> entity) {
        Map<String, Object> extra = params.getExtra();
        if (CollUtil.isEmpty(extra)) {
            return;
        }

        extra.forEach((property, value) -> {
            if (ObjectUtil.isEmpty(value)) {
                return;
            }
            // Java字段
            String beanProperty = StrUtil.subBefore(property, UNDERSCORE, false);
            // 操作符
            String operator = StrUtil.subAfter(property, UNDERSCORE, false);

            // 数据库字段
            String column = getColumnByProperty(beanProperty, entity);

            switch (operator) {
                case ST:
                    wrapper.ge(column, DateUtils.getStartTime(value.toString()));
                    break;
                case ED:
                    wrapper.le(column, DateUtils.getEndTime(value.toString()));
                    break;
                case START:
                    wrapper.ge(column, DateUtils.parseAsLocalDateTime(value.toString()));
                    break;
                case END:
                    wrapper.le(column, DateUtils.parseAsLocalDateTime(value.toString()));
                    break;
                case GE:
                    wrapper.ge(column, value);
                    break;
                case GT:
                    wrapper.gt(column, value);
                    break;
                case LT:
                    wrapper.lt(column, value);
                    break;
                case LE:
                    wrapper.le(column, value);
                    break;
                case EQ:
                    wrapper.eq(column, value);
                    break;
                case NE:
                    wrapper.ne(column, value);
                    break;
                case LIKE_LEFT:
                    wrapper.likeLeft(column, value);
                    break;
                case LIKE_RIGHT:
                    wrapper.likeRight(column, value);
                    break;
                case IN:
                    wrapper.in(column, value instanceof Collection<?> coll ? coll : null);
                    break;
                case LIKE:
                default:
                    wrapper.like(column, value);
                    break;
            }
        });

    }

    /**
     * 根据分页参数，构建排序条件
     * @param wrapper 查询条件
     * @param params 分页参数
     * @param entity 实体类
     * @param <Query> 业务参数
     */
    public static <Query> void buildWrapperByOrder(QueryWrapper wrapper, PageParams<Query> params, Class<?> entity) {
        List<String> sortArr = StrUtil.split(params.getSort(), StrPool.COMMA);
        List<String> orderArr = StrUtil.split(params.getOrder(), StrPool.COMMA);

        int len = Math.min(sortArr.size(), orderArr.size());
        for (int i = 0; i < len; i++) {
            String humpSort = sortArr.get(i);
            String order = orderArr.get(i);

            String beanColumn = getColumnByProperty(humpSort, entity);
            wrapper.orderBy(new QueryColumn(beanColumn), StrUtil.equalsAny(order, "ascending", "ascend"));
        }
    }

    /**
     * 将 bean字段 转换为 数据库字段
     *
     * @param beanProperty 字段
     * @param clazz     类型
     * @return 数据库字段名
     */

    public static String getColumnByProperty(String beanProperty, Class<?> clazz) {
        ArgumentAssert.notNull(clazz, "实体类不能为空");
        ArgumentAssert.notEmpty(beanProperty, "字段名不能为空");
        TableInfo tableInfo = TableInfoFactory.ofEntityClass(clazz);

        String column = tableInfo.getColumnByProperty(beanProperty);

        if (!ArrayUtil.contains(tableInfo.getAllColumns(), column)) {
            throw BizException.wrap("实体类{} 中没有字段：{}， 排序请传递实体类的字段名，而非数据库字段名", clazz.getSimpleName(), beanProperty);
        }
        return column;
    }
}
