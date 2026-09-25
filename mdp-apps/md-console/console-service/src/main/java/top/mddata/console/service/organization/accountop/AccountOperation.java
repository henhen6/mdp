package top.mddata.console.service.organization.accountop;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账号管理操作。
 */
@Getter
@AllArgsConstructor
public enum AccountOperation {
    /**
     * 禁用账号
     */
    DISABLE("禁用"),
    /**
     * 启用账号
     */
    ENABLE("启用"),
    /**
     * 重置密码
     */
    RESET_PASSWORD("重置密码"),
    /**
     * 删除账号
     */
    DELETE("删除");

    /**
     * 操作描述（用于错误消息）
     */
    private final String desc;
}
