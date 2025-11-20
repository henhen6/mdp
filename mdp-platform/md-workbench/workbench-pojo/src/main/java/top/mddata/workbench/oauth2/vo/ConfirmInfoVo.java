package top.mddata.workbench.oauth2.vo;

import lombok.Data;
import top.mddata.open.admin.vo.AppVo;
import top.mddata.open.admin.vo.OauthScopeVo;
import top.mddata.workbench.vo.SsoUserVo;

import java.util.List;

/**
 * oauth2 应用确认信息
 * @author henhen6
 * @since 2025/8/20 00:25
 */
@Data
public class ConfirmInfoVo {
    private AppVo appVo;
    private List<String> scopes;
    private List<OauthScopeVo> scopeList;
    private SsoUserVo user;
}
