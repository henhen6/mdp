package top.mddata.base.mvcflex.request;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mybatisflex.core.paginate.Page;
import top.mddata.base.utils.BeanPlusUtil;

import java.util.List;
import java.util.Map;

/**
 * 分页工具类
 *
 * @author henhen6
 * @since 2020/11/26 10:25 上午
 */
public class PageFlexUtil {
    private PageFlexUtil() {
    }

    /**
     * 转化Page 对象
     *
     * @param page             分页对象
     * @param destinationClass 目标类型
     * @return 目录分页对象
     */
    public static <T, E> Page<T> toBeanPage(Page<E> page, Class<T> destinationClass) {
        if (page == null || destinationClass == null) {
            return null;
        }
        Page<T> newPage = new Page<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow());
        newPage.setTotalPage(page.getTotalPage());

        List<E> list = page.getRecords();
        if (CollUtil.isEmpty(list)) {
            return newPage;
        }

        List<T> destinationList = BeanPlusUtil.toBeanList(list, destinationClass);

        newPage.setRecords(destinationList);
        return newPage;
    }

    /**
     * 重置时间区间参数
     *
     * @param params 分页参数
     */
    public static <T> void timeRange(PageParams<T> params) {
        if (params == null) {
            return;
        }
        Map<String, Object> extra = params.getExtra();
        if (MapUtil.isEmpty(extra)) {
            return;
        }
        for (Map.Entry<String, Object> field : extra.entrySet()) {
            String key = field.getKey();
            Object value = field.getValue();
            if (ObjectUtil.isEmpty(value)) {
                continue;
            }
//            if (key.endsWith(Wraps.ST)) {
//                extra.put(key, DateUtils.getStartTime(value.toString()));
//            } else if (key.endsWith(Wraps.ED)) {
//                extra.put(key, DateUtils.getEndTime(value.toString()));
//            }
        }
    }
}
