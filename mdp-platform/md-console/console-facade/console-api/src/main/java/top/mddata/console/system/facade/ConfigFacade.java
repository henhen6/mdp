package top.mddata.console.system.facade;

import top.mddata.console.system.vo.ConfigVo;

/**
 * 系统参数
 * @author henhen6
 * @since 2025/10/10 23:54
 */
public interface ConfigFacade {

    /**
     * 根据参数标识，查询系统参数VO
     * @param uniqKey 参数标识
     * @return 系统参数
     */
    ConfigVo getConfig(String uniqKey);

    /**
     * 根据参数标识，查询长整型系统参数
     * @param uniqKey 参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    Long getLong(String uniqKey, Long defaultValue);

    /**
     * 根据参数标识，查询整型系统参数
     * @param uniqKey 参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    Integer getInteger(String uniqKey, Integer defaultValue);

    /**
     * 根据参数标识，查询字符型系统参数
     * @param uniqKey 参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    String getString(String uniqKey, String defaultValue);

    /**
     * 根据参数标识，查询布尔型系统参数
     * @param uniqKey 参数标识
     * @param defaultValue 默认值
     * @return 参数值
     */
    Boolean getBoolean(String uniqKey, Boolean defaultValue);
}
