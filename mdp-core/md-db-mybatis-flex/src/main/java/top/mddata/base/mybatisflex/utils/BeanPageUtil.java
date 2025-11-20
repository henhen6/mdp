package top.mddata.base.mybatisflex.utils;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.paginate.Page;
import top.mddata.base.utils.BeanPlusUtil;

import java.util.List;

/**
 * Bean增强类工具类
 *
 * <p>
 * 把一个拥有对属性进行set和get方法的类，我们就可以称之为JavaBean。
 * </p>
 *
 * @author henhen6
 * @since 3.1.2
 */
public class BeanPageUtil extends BeanPlusUtil {


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

        List<T> destinationList = toBeanList(list, destinationClass);

        newPage.setRecords(destinationList);
        return newPage;
    }

}
