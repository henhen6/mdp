package top.mddata.open.facade.admin.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import top.mddata.base.base.R;
import top.mddata.common.constant.AppConstants;
import top.mddata.open.vo.admin.OauthScopeVo;

import java.util.List;

/**
 *
 * @author henhen
 * @since 2026/5/10 23:07
 */
@FeignClient(name = AppConstants.OPEN_SERVER, path = "/inner/oauthScope")
public interface ScopeApi {
    /**
     * 根据应用id查询应用拥有的权限
     *
     * @param appId 应用id
     * @return 权限
     */
    @GetMapping("/listByAppId")
    R<List<OauthScopeVo>> listByAppId(@RequestParam Long appId);

    /**
     * 根据权限编码查询 应用权限
     *
     * @param scopes 权限编码
     * @return 应用权限
     */
    @PostMapping("/getScopeListByCode")
    R<List<OauthScopeVo>> getScopeListByCode(@RequestBody List<String> scopes);
}
