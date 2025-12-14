package top.mddata.workbench.oauth2.controller;

import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.config.SaOAuth2ServerConfig;
import cn.dev33.satoken.oauth2.consts.GrantType;
import cn.dev33.satoken.oauth2.consts.SaOAuth2Consts;
import cn.dev33.satoken.oauth2.data.generate.SaOAuth2DataGenerate;
import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.data.model.CodeModel;
import cn.dev33.satoken.oauth2.data.model.loader.SaClientModel;
import cn.dev33.satoken.oauth2.data.model.request.ClientIdAndSecretModel;
import cn.dev33.satoken.oauth2.data.model.request.RequestAuthModel;
import cn.dev33.satoken.oauth2.error.SaOAuth2ErrorCode;
import cn.dev33.satoken.oauth2.exception.SaOAuth2Exception;
import cn.dev33.satoken.oauth2.strategy.SaOAuth2Strategy;
import cn.dev33.satoken.oauth2.template.SaOAuth2Template;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mddata.base.base.R;
import top.mddata.base.exception.BizException;
import top.mddata.base.utils.ArgumentAssert;
import top.mddata.base.utils.BeanPlusUtil;
import top.mddata.base.utils.ContextUtil;
import top.mddata.common.entity.User;
import top.mddata.open.manage.facade.AppFacade;
import top.mddata.open.admin.vo.AppVo;
import top.mddata.open.admin.vo.OauthScopeVo;
import top.mddata.workbench.dto.LoginDto;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.oauth2.dto.ConfirmDto;
import top.mddata.workbench.oauth2.dto.RedirectUriDto;
import top.mddata.workbench.oauth2.vo.ConfirmInfoVo;
import top.mddata.workbench.oauth2.vo.ConfirmVo;
import top.mddata.workbench.service.AuthService;
import top.mddata.workbench.service.SsoUserService;
import top.mddata.workbench.vo.SsoUserVo;

import java.util.List;

import static top.mddata.workbench.oauth2.data.Oauth2DataResolver.readClientIdAndSecret;

/**
 * OAuth2 Server端
 *
 * @date 2025年08月19日12:40:34
 * @author henhen6
 */
@Slf4j
@RestController
@RequestMapping
@AllArgsConstructor
@Tag(name = "Oauth2登录", description = "此模块只处理Oauth2登录路由跳转")
public class OAuth2ServerController {
    @Autowired
    private AppFacade appFacade;
    @Autowired
    private SsoUserService ssoUserService;
    @Autowired
    private AuthService authService;

    /**
     * 获取授权重定向地址
     *
     * 【ua-center登录页面调用】
     *
     * @return
     */
    @PostMapping("/oauth2/getRedirectUri")
    public R<String> getRedirectUri(@Validated RedirectUriDto param) {
        // 获取变量
        SaOAuth2ServerConfig cfg = SaOAuth2Manager.getServerConfig();
        SaOAuth2DataGenerate dataGenerate = SaOAuth2Manager.getDataGenerate();
        SaOAuth2Template oauth2Template = SaOAuth2Manager.getTemplate();
        String responseType = param.getResponseType();

        // 1、先判断是否开启了指定的授权模式
        checkAuthorizeResponseType(responseType, param, cfg);

        // 2、如果尚未登录, 则先去登录
        long loginId = SaOAuth2Manager.getStpLogic().getLoginIdAsLong();

        // 3、构建请求 Model
        RequestAuthModel ra = readRequestAuthModel(param, loginId);

        // 4、开发者自定义的授权前置检查
        SaOAuth2Strategy.instance.userAuthorizeClientCheck.run(ra.getLoginId(), ra.getClientId());

        // 5、校验：重定向域名是否合法
        oauth2Template.checkRedirectUri(ra.getClientId(), ra.getRedirectUri());

        // 6、校验：此次申请的Scope，该Client是否已经签约
        oauth2Template.checkContractScope(ra.getClientId(), ra.getScopes());

        // 7、判断：如果此次申请的Scope，该用户尚未授权，则转到授权页面
        boolean isNeedCarefulConfirm = oauth2Template.isNeedCarefulConfirm(ra.getLoginId(), ra.getClientId(), ra.getScopes());
        if (isNeedCarefulConfirm) {
            SaClientModel cm = oauth2Template.checkClientModel(ra.getClientId());
            if (!cm.getIsAutoConfirm()) {
                // code=411，需要用户手动确认授权
                return R.fail(411, "请手动确认授权");
            }
        }

        // 8、判断授权类型，重定向到不同地址
        //         如果是 授权码式，则：开始重定向授权，下放code
        if (SaOAuth2Consts.ResponseType.code.equals(ra.getResponseType())) {
            CodeModel codeModel = dataGenerate.generateCode(ra);
            String redirectUri = dataGenerate.buildRedirectUri(ra.getRedirectUri(), codeModel.getCode(), ra.getState());
            return R.success(redirectUri);
        }

        // 如果是 隐藏式，则：开始重定向授权，下放 token
        if (SaOAuth2Consts.ResponseType.token.equals(ra.getResponseType())) {
            AccessTokenModel at = dataGenerate.generateAccessToken(ra, false, null);
            String redirectUri = dataGenerate.buildImplicitRedirectUri(ra.getRedirectUri(), at.getAccessToken(), ra.getState());
            return R.success(redirectUri);
        }

        // 默认返回
        throw new SaOAuth2Exception("无效 response_type: " + ra.responseType).setCode(SaOAuth2ErrorCode.CODE_30125);
    }

    /**
     * 根据当前请求提交的 client_id 参数获取 SaClientModel 对象
     * @return /
     */
    private SaClientModel currClientModel(RedirectUriDto param) {
        SaOAuth2Template oauth2Template = SaOAuth2Manager.getTemplate();
        ClientIdAndSecretModel clientIdAndSecret = readClientIdAndSecret(param);
        return oauth2Template.checkClientModel(clientIdAndSecret.getClientId());
    }


    /**
     * 校验 authorize 路由的 ResponseType 参数
     */
    private void checkAuthorizeResponseType(String responseType, RedirectUriDto param, SaOAuth2ServerConfig cfg) {
        // 模式一：Code授权码
        if (responseType.equals(SaOAuth2Consts.ResponseType.code)) {
            if (!cfg.enableAuthorizationCode) {
                throwErrorSystemNotEnableModel();
            }
            if (!currClientModel(param).getAllowGrantTypes().contains(GrantType.authorization_code)) {
                throwErrorClientNotEnableModel();
            }
        } else if (responseType.equals(SaOAuth2Consts.ResponseType.token)) {
            // 模式二：隐藏式
            if (!cfg.enableImplicit) {
                throwErrorSystemNotEnableModel();
            }
            if (!currClientModel(param).getAllowGrantTypes().contains(GrantType.implicit)) {
                throwErrorClientNotEnableModel();
            }
        } else {
            // 其它
            throw new SaOAuth2Exception("无效 response_type: " + responseType).setCode(SaOAuth2ErrorCode.CODE_30125);
        }
    }

    /**
     * 系统未开放此授权模式时抛出异常
     */
    private void throwErrorSystemNotEnableModel() {
        throw new SaOAuth2Exception("系统暂未开放此授权模式").setCode(SaOAuth2ErrorCode.CODE_30141);
    }

    /**
     * 应用未开放此授权模式时抛出异常
     */
    private void throwErrorClientNotEnableModel() {
        throw new SaOAuth2Exception("应用暂未开放此授权模式").setCode(SaOAuth2ErrorCode.CODE_30142);
    }

    private RequestAuthModel readRequestAuthModel(RedirectUriDto param, Object loginId) {
        RequestAuthModel ra = new RequestAuthModel();
        ra.setClientId(param.getClientId());
        ra.setResponseType(param.getResponseType());
        ra.setRedirectUri(param.getRedirectUri());
        ra.setState(param.getState());
        ra.setNonce(param.getNonce());
        ra.setScopes(SaOAuth2Manager.getDataConverter().convertScopeStringToList(param.getScope()));
        ra.setLoginId(loginId);
        return ra;
    }

    /**
     * 查询在授权页需要显示的信息
     * 【ua-center】登录后，确认授权页面调用
     */
    @PostMapping("/oauth2/getConfirmInfo")
    public R<ConfirmInfoVo> getConfirmInfo(@RequestParam(value = "client_id") String clientId, @RequestParam String scope) {

        // 查询应用信息
        R<AppVo> result = appFacade.getAppByAppKey(clientId);
        if (!result.getIsSuccess()) {
            return R.fail("查询应用失败");
        }
        AppVo opApplication = result.getData();
        ArgumentAssert.notNull(opApplication, "应用不存在");

        ConfirmInfoVo vo = new ConfirmInfoVo();
        // 查询权限信息
        List<String> scopes = SaOAuth2Manager.getDataConverter().convertScopeStringToList(scope);
        R<List<OauthScopeVo>> scopeList = appFacade.getScopeListByCode(scopes);


        User ssoUser = ssoUserService.getById(ContextUtil.getUserId());
        vo.setScopes(scopes);
        vo.setScopeList(scopeList.getData());
        vo.setAppVo(opApplication);
        vo.setUser(BeanPlusUtil.toBean(ssoUser, SsoUserVo.class));

        return R.success(vo);
    }

    /**
     * 确认授权
     * 【ua-center】登录后，确认授权页面调用
     * @return 重定向到客户端的地址
     */
    @PostMapping("/oauth2/confirm")
    public R<ConfirmVo> confirm(@Validated ConfirmDto param) {
        // 获取变量
        Object loginId = SaOAuth2Manager.getStpLogic().getLoginId();
        String clientId = param.getClientId();
        String scope = param.getScope();
        List<String> scopes = SaOAuth2Manager.getDataConverter().convertScopeStringToList(scope);
        SaOAuth2DataGenerate dataGenerate = SaOAuth2Manager.getDataGenerate();
        SaOAuth2Template oauth2Template = SaOAuth2Manager.getTemplate();

        // 确认授权
        oauth2Template.saveGrantScope(clientId, loginId, scopes);

        // 判断所需的返回结果模式
        boolean buildRedirectUri = param.getBuildRedirectUri();

        ConfirmVo vo = new ConfirmVo();
        // -------- 情况1：只返回确认结果即可
        if (!buildRedirectUri) {
            oauth2Template.saveGrantScope(clientId, loginId, scopes);
            return R.success(vo);
        }

        // -------- 情况2：需要返回最终的 redirect_uri 地址

        // 构建请求 Model
        RequestAuthModel ra = new RequestAuthModel();
        ra.setClientId(param.getClientId())
                .setResponseType(param.getResponseType())
                .setRedirectUri(param.getRedirectUri())
                .setState(param.getState())
                .setNonce(param.getNonce())
                .setScopes(scopes)
                .setLoginId(loginId);

        // 判断授权类型，构建不同的重定向地址
        // 		如果是 授权码式，则：开始重定向授权，下放code
        if (SaOAuth2Consts.ResponseType.code.equals(ra.getResponseType())) {
            CodeModel codeModel = dataGenerate.generateCode(ra);
            String redirectUri = dataGenerate.buildRedirectUri(ra.getRedirectUri(), codeModel.getCode(), ra.getState());
            vo.setRedirectUri(redirectUri);
            return R.success(vo);
        }

        // 		如果是 隐藏式，则：开始重定向授权，下放 token
        if (SaOAuth2Consts.ResponseType.token.equals(ra.getResponseType())) {
            AccessTokenModel at = dataGenerate.generateAccessToken(ra, false, null);
            String redirectUri = dataGenerate.buildImplicitRedirectUri(ra.getRedirectUri(), at.getAccessToken(), ra.getState());
            vo.setRedirectUri(redirectUri);
            return R.success(vo);
        }

        // 默认返回
        throw new SaOAuth2Exception("无效response_type: " + ra.getResponseType()).setCode(SaOAuth2ErrorCode.CODE_30125);
    }

    /**
     * OAuth2 定制化配置
     * @param oauth2Server 配置
     */
    @Autowired
    public void configOAuth2Server(SaOAuth2ServerConfig oauth2Server) {

        // 确认授权时返回的 view
        SaOAuth2Strategy.instance.confirmView = (clientId, scopes) -> {
            throw new BizException("暂无此功能");
        };
        // 未登录时返回的View
        SaOAuth2Strategy.instance.notLoginView = () -> {
            throw new BizException("暂无此功能");
        };

        // 登录处理函数
        SaOAuth2Strategy.instance.doLoginHandle = (name, pwd) -> {
            LoginDto loginDto = new LoginDto();
            loginDto.setUsername(name);
            loginDto.setPassword(pwd);
            loginDto.setAuthType(AuthTypeEnum.USERNAME);
            return authService.login(loginDto);
        };

    }


}
