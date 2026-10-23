package top.mddata.open.facade.admin.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.open.vo.admin.OauthOpenidVo;

/**
 *
 * @author henhen
 * @since 2026/5/11 09:12
 */
@FeignClient(name = AppConstants.OPEN_SERVER, path = "/inner/oauthOpenid")
public interface OauthOpenidApi {
    /**
     * 根据应用id和用户id 查询union
     *
     * @param appKey  应用id
     * @param userId 用户id
     * @return union
     */
    @GetMapping("/getByAppIdAndUserId")
    R<OauthOpenidVo> getByAppKeyAndUserId(@RequestParam String appKey, @RequestParam Long userId);
}
