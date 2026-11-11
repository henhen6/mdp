package top.mddata.workbench.service;

import top.mddata.common.entity.User;
import top.mddata.workbench.enumeration.AuthTypeEnum;
import top.mddata.workbench.vo.LoginVo;

/**
 * 已认证用户会话签发服务。
 *
 * <p>账号登录与社交登录共用：调用方必须先完成身份认证，本接口只负责
 * Sa-Token 登录、TokenSession 写入、LoginVo 封装与登录成功日志，不再校验凭证。</p>
 *
 * @author tangyh
 */
public interface LoginSessionService {

    /**
     * 为已认证用户签发会话。
     *
     * @param user       已认证用户
     * @param authType   认证类型（用于登录日志）
     * @param account    登录账号（用于登录日志）
     * @param deviceId   客户端设备 id，为空时自动生成
     * @param deviceInfo 设备信息
     * @param deviceType 设备类型
     * @return 登录返回信息
     */
    LoginVo loginAuthenticated(User user, AuthTypeEnum authType, String account,
                               String deviceId, String deviceInfo, String deviceType);
}
