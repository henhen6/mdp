package top.mddata.workbench.service.social;

import top.mddata.workbench.dto.SocialCallbackDto;
import top.mddata.workbench.dto.SocialReauthDto;
import top.mddata.workbench.dto.SocialReauthUrlDto;
import top.mddata.workbench.dto.SocialTicketDto;
import top.mddata.workbench.dto.SocialTransferDto;
import top.mddata.workbench.dto.SocialUnbindDto;
import top.mddata.workbench.enumeration.SocialProviderEnum;
import top.mddata.workbench.vo.LoginVo;
import top.mddata.workbench.vo.SocialBindingVo;
import top.mddata.workbench.vo.SocialReauthTicketVo;

import java.util.List;

/**
 * 个人中心第三方账号管理服务。
 *
 * <p>绑定、解绑和跨账号转移都属于敏感操作，必须先在服务内完成二次验证；
 * 入口控制器只负责从认证上下文取得当前用户，禁止接受前端传入的用户 ID。</p>
 *
 * @author tangyh
 */
public interface SocialAccountService {

    /**
     * 查询当前用户第三方账号绑定状态
     *
     * @param userId 当前用户ID
     * @return 各平台绑定状态
     */
    List<SocialBindingVo> list(Long userId);

    /**
     * 通过当前 MDP 账号凭据完成二次验证并签发票据
     *
     * @param userId 当前用户ID
     * @param dto    凭据与操作目标
     * @return 二次验证票据
     */
    SocialReauthTicketVo reauth(Long userId, SocialReauthDto dto);

    /**
     * 生成第三方重新授权地址，用于证明拥有当前绑定平台的身份
     *
     * @param userId        当前用户ID
     * @param reauthProvider 用于重新授权的已绑定平台
     * @param dto           操作目标
     * @return 第三方授权地址
     */
    String reauthUrl(Long userId, SocialProviderEnum reauthProvider, SocialReauthUrlDto dto);

    /**
     * 处理第三方重新授权回调并签发二次验证票据
     *
     * @param userId   当前用户ID
     * @param callback 授权回调
     * @return 二次验证票据
     */
    SocialReauthTicketVo reauthByCallback(Long userId, SocialCallbackDto callback);

    /**
     * 校验二次验证票据并生成目标平台绑定授权地址
     *
     * @param userId   当前用户ID
     * @param provider 目标平台
     * @param dto      二次验证票据
     * @return 目标平台授权地址
     */
    String bindUrl(Long userId, SocialProviderEnum provider, SocialTicketDto dto);

    /**
     * 完成第三方账号绑定
     *
     * @param userId   当前用户ID
     * @param callback 授权回调
     */
    void bind(Long userId, SocialCallbackDto callback);

    /**
     * 解绑第三方账号，并保护最后一种可用登录方式
     *
     * @param userId 当前用户ID
     * @param dto    解绑入参
     */
    void unbind(Long userId, SocialUnbindDto dto);

    /**
     * 误注册后的跨账号安全转移：把当前账号的第三方身份转移到目标账号
     *
     * <p>不迁移用户资料、组织、角色等任何业务数据，仅修改绑定记录并切换到目标账号会话。</p>
     *
     * @param currentUserId 当前（误注册）账号ID
     * @param dto           转移入参
     * @return 目标账号登录信息
     */
    LoginVo transfer(Long currentUserId, SocialTransferDto dto);
}