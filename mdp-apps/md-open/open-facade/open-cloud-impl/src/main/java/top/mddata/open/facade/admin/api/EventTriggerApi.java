package top.mddata.open.facade.admin.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.open.dto.admin.EventTriggerDto;
import top.mddata.open.entity.admin.EventTrigger;

/**
 *
 * @author henhen
 * @since 2026/5/11 09:22
 */
@FeignClient(name = AppConstants.OPEN_SERVER)
public interface EventTriggerApi {
    /**
     * 保存事件触发
     *
     * @param save 事件触发参数
     * @return 事件触发
     */
    @PostMapping("/inner/eventTrigger/save")
    R<EventTrigger> save(@RequestBody EventTriggerDto save);
}
