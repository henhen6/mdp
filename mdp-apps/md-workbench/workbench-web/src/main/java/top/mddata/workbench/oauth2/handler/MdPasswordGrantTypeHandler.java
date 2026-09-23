package top.mddata.workbench.oauth2.handler;

import cn.dev33.satoken.oauth2.error.SaOAuth2ErrorCode;
import cn.dev33.satoken.oauth2.exception.SaOAuth2Exception;
import cn.dev33.satoken.oauth2.granttype.handler.PasswordGrantTypeHandler;
import cn.dev33.satoken.oauth2.granttype.handler.model.PasswordAuthResult;
import cn.dev33.satoken.oauth2.strategy.SaOAuth2Strategy;
import cn.dev33.satoken.stp.StpUtil;

/**
 * 密码模式处理器（替换 sa-token 默认实现）
 * <p>
 * 默认实现的 loginByUsernamePassword 每次调用都会向 stderr 打印"仅供开发测试"警告，
 * 且登录失败后只返回笼统的"登录失败"。本实现复用 {@code doLoginHandle} 的登录逻辑与错误转换，
 * 并消除警告日志
 *
 * @author henhen6
 */
public class MdPasswordGrantTypeHandler extends PasswordGrantTypeHandler {

    @Override
    public PasswordAuthResult loginByUsernamePassword(String username, String password) {
        // 登录失败时 doLoginHandle 会抛出携带可读信息的 SaOAuth2Exception（invalid_grant），直接向上传递
        SaOAuth2Strategy.instance.doLoginHandle.apply(username, password);
        Object loginId = StpUtil.getLoginIdDefaultNull();
        if (loginId == null) {
            // 兜底：doLoginHandle 未抛异常但会话未建立，按凭证错误处理
            throw new SaOAuth2Exception("用户名或密码错误").setCode(SaOAuth2ErrorCode.CODE_30161);
        }
        return new PasswordAuthResult(loginId);
    }
}
