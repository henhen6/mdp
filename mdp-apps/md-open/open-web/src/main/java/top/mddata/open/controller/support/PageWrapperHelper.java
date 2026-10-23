package top.mddata.open.controller.support;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.query.QueryWrapper;
import top.mddata.base.base.ExtraParams;
import top.mddata.base.mvcflex.request.PageParams;
import top.mddata.base.mvcflex.utils.WrapperUtil;

/**
 * 分页查询 Wrapper 构建：实体映射 + 操作符 + 扩展条件 + 排序，四步合一。
 * 消除各 Controller page 方法的模板代码重复。
 */
public final class PageWrapperHelper {

    private PageWrapperHelper() {
    }

    public static <T> QueryWrapper build(PageParams<? extends ExtraParams> params, Class<T> entityClass) {
        T entity = BeanUtil.toBean(params.getModel(), entityClass);
        QueryWrapper wrapper = QueryWrapper.create(entity, WrapperUtil.buildOperators(entityClass));
        WrapperUtil.buildWrapperByExtra(wrapper, params.getModel(), entityClass);
        WrapperUtil.buildWrapperByOrder(wrapper, params, entityClass);
        return wrapper;
    }
}
