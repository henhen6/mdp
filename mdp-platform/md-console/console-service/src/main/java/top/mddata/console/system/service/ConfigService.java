package top.mddata.console.system.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.system.entity.Config;
import top.mddata.console.system.vo.ConfigVo;

import java.util.List;
import java.util.Map;

/**
 * 系统配置 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
public interface ConfigService extends SuperService<Config> {
    /**
     * 检查参数标识是否重复
     *
     * @param uniqKey 标识
     * @param orgId   组织id
     * @param id      参数id
     * @return true 表示已存在；false 表示不存在
     */
    Boolean check(String uniqKey, Long orgId, Long id);

    /**
     * 根据参数标识，查询系统参数
     *
     * @param uniqKey 参数标识
     * @return 系统参数
     */
    Config getByUniqKey(String uniqKey);

    /**
     * 根据参数标识，查询系统参数VO
     *
     * @param uniqKey 参数标识
     * @return 系统参数
     */
    ConfigVo getParam(String uniqKey);

    /**
     * 根据参数标识，查询长整型系统参数
     *
     * @param uniqKey      参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    Long getLong(String uniqKey, Long defaultValue);

    /**
     * 根据参数标识，查询整型系统参数
     *
     * @param uniqKey      参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    Integer getInteger(String uniqKey, Integer defaultValue);

    /**
     * 根据参数标识，查询字符型系统参数
     *
     * @param uniqKey      参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    String getString(String uniqKey, String defaultValue);

    /**
     * 根据参数标识，查询布尔型系统参数
     *
     * @param uniqKey      参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    Boolean getBoolean(String uniqKey, Boolean defaultValue);

    /**
     * 根据参数标识，查询参数
     *
     * @param uniqKeys 参数标识
     * @return 参数
     */
    Map<String, ConfigVo> findParamByUniqKey(List<String> uniqKeys);
}
