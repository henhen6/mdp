package top.mddata.open.facade.admin.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.open.dto.admin.NotifyInfoDto;

/**
 *
 * @author henhen
 * @since 2026/5/11 09:22
 */
@FeignClient(name = AppConstants.OPEN_SERVER)
public interface NotifyInfoApi {
    /**
     * 回调
     *
     * @param request 回调内容
     * @return 返回回调id
     */
    @PostMapping("/inner/notifyInfo/notify")
    R<Long> notify(@RequestBody NotifyInfoDto request);

}
