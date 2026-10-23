package top.mddata.open.facade.admin.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.open.dto.admin.OauthLogDto;

/**
 * 授权记录
 *
 * @author henhen6
 * @since 2025/8/21 23:33
 */
@FeignClient(name = AppConstants.OPEN_SERVER, path = "/inner/oauthLog")
public interface OauthLogApi {
    /**
     * 保存授权记录
     *
     * @param dlt 记录
     * @return 记录ID
     */
    @PostMapping("/save")
    R<Long> save(@RequestBody OauthLogDto dlt);
}
