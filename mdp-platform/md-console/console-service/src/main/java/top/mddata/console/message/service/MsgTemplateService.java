package top.mddata.console.message.service;

import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.message.entity.MsgTemplate;

/**
 * 消息模板 服务层。
 *
 * @author henhen6
 * @since 2025-12-21 00:12:48
 */
public interface MsgTemplateService extends SuperService<MsgTemplate> {

    /**
     * 检查key是否可用
     * @param key 模板标识
     * @param id id
     * @return true 存在， false 不存在
     */
    Boolean check(String key, Long id);

    /**
     * 根据模板标识获取模板
     * @param templateKey 模板标识
     * @return 模板
     */
    MsgTemplate getByTemplateKey(String templateKey);
}
