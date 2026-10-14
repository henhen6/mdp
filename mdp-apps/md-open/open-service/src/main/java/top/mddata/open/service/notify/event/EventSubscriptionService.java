package top.mddata.open.service.notify.event;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.open.entity.admin.EventSubscription;
import top.mddata.open.vo.admin.AppKeysVo;

import java.util.List;

/**
 * 事件订阅 服务层。
 *
 * @author henhen6
 * @since 2026-01-02 10:13:39
 */
public interface EventSubscriptionService extends SuperService<EventSubscription> {
    /**
     * 根据应用ID获取请用拥有的事件类型ID列表
     *
     * @param appId 应用ID
     * @return 事件类型ID列表
     */
    List<Long> listEventTypeIdByAppId(Long appId);

    /**
     * 保存事件订阅信息
     *
     * @param appId 应用ID
     * @param eventTypeIdList 事件类型ID列表
     */
    void saveEventSubscriptionByAppId(Long appId, List<Long> eventTypeIdList);

    /**
     * 查询订阅了指定事件、且通知开关打开、应用启用的应用密钥列表（含加密配置）。
     *
     * @param eventCode 事件编码
     * @return 订阅方密钥列表
     */
    List<AppKeysVo> findByEventCode(String eventCode);
}
