package top.mddata.workbench.oauth2.handler;

import cn.dev33.satoken.oauth2.data.model.oidc.IdTokenModel;
import cn.dev33.satoken.oauth2.scope.handler.OidcScopeHandler;
import cn.dev33.satoken.util.SaFoxUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.mddata.common.entity.User;
import top.mddata.workbench.service.SsoUserService;

/**
 * 扩展 OIDC 权限处理器，返回更多字段
 *
 * 参考： https://sa-token.cc/doc.html#/oauth2/oauth2-oidc?id=oauth2-%e5%bc%80%e5%90%af-oidc-%e5%8d%8f%e8%ae%ae-%ef%bc%88openid-connect%ef%bc%89
 * @author henhen6
 * @since 2025年11月13日10:27:53
 */
@Component
public class CustomOidcScopeHandler extends OidcScopeHandler {
    @Autowired
    private SsoUserService ssoUserService;

    /**
     * 为 idToken 追加扩展字段
     * @param idToken  idToken
     * @return 实体
     */
    @Override
    public IdTokenModel workExtraData(IdTokenModel idToken) {
        long userId = SaFoxUtil.getValueByType(idToken.sub, Long.class);
        User user = ssoUserService.getById(userId);
        idToken.extraData.put("uid", user.getId()); // 用户id
        idToken.extraData.put("nickname", user.getUsername()); // 昵称
        idToken.extraData.put("avatar", user.getAvatar()); // 头像
        idToken.extraData.put("email", user.getEmail()); // 邮箱
        idToken.extraData.put("phone", user.getPhone()); // 手机号

        return idToken;
    }

}
