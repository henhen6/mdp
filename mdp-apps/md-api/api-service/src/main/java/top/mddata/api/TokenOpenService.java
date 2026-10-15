package top.mddata.api;

import com.gitee.sop.support.annotation.Open;
import top.mddata.api.dto.AccessTokenDto;
import top.mddata.api.resp.AccessTokenResp;

/**
 * 访问令牌接口
 *
 * @author henhen
 * @since 2026/7/1
 */
public interface TokenOpenService {

    /**
     * 获取访问令牌
     *
     * @apiNote 通过 appKey + appSecret 换取 accessToken。 若已有未过期 token，直接返回（除非 forceRefresh=true）；否则生成新 token 并缓存（默认有效期 2 小时）。
     * @param dto 入参（appKey、appSecret、forceRefresh）
     * @return accessToken 响应（含 accessToken 和 expiresIn）
     */
    @Open(value = "accessToken.get", needToken = false, needSign = true)
    AccessTokenResp get(AccessTokenDto dto);
}
