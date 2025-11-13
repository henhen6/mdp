package top.mddata.open.manage.facade;

import top.mddata.common.vo.Option;

import java.util.List;
import java.util.Map;

/**
 *
 * @author henhen6
 * @since 2025/9/23 20:40
 */
public interface OpenEnumFacade {
    /**
     * 查找本服务中，所有的枚举类
     *
     * @param rescan 是否重新扫描
     * @return 枚举数据
     */
    Map<Option, List<Option>> findAll(Boolean rescan);

}
