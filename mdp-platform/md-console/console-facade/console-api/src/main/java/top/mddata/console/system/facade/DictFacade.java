package top.mddata.console.system.facade;

import top.mddata.base.interfaces.echo.LoadService;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 字典接口
 * @author henhen6
 * @since 2025/7/27 00:39
 */
public interface DictFacade extends LoadService {

    /**
     * 根据字典key###字典项id查询 字典项值
     *
     * @param ids 字典项id
     * @return 字典项
     */
    @Override
    Map<Serializable, Object> findByIds(Set<Serializable> ids);

}
