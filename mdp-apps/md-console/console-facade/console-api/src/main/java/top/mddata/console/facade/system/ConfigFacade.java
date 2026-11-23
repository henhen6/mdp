package top.mddata.console.facade.system;

import top.mddata.console.vo.system.ConfigVo;

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

    /**
     * 严格读取布尔型系统参数，仅供安全敏感调用使用。
     * <p>
     * 与 {@link #getBoolean(String, Boolean)} 不同：远程调用失败或响应异常时不回退默认值，
     * 直接抛出业务异常，避免安全策略因读取失败而被静默放行。
     *
     * @param uniqKey 参数标识
     * @param defaultValue 默认值（仅在服务端正常响应且值为空时使用）
     * @return 参数值
     */
    Boolean getBooleanRequired(String uniqKey, Boolean defaultValue);
}
