package top.mddata.open.facade.admin.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.open.vo.admin.AppKeysVo;

/**
 * 应用秘钥接口
 *
 * @author henhen6
 * @since 2026/06/29
 */
@FeignClient(name = AppConstants.OPEN_SERVER, path = "/inner/app")
public interface AppKeysApi {

    /**
     * 根据appKey查询应用秘钥（包含通知加密配置）
     *
     * @param appKey 应用标识
     * @return 秘钥VO
     */
    @GetMapping("/getAppKeysByAppKey")
    R<AppKeysVo> getAppKeysByAppKey(@RequestParam String appKey);
}
