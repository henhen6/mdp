package top.mddata.console.message.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.message.entity.InterfaceStat;

/**
 * 接口统计 服务层。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
public interface InterfaceStatService extends SuperService<InterfaceStat> {

    /**
     * 递增失败次数
     *
     * @param id 接口统计ID
     */
    void incrFailCount(Long id);

    /**
     * 递增成功次数
     *
     * @param id 接口统计ID
     */
    void incrSuccessCount(Long id);
}
