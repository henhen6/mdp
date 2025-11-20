package top.mddata.common.constant;

/**
 * 配置表中配置的参数标识
 *
 * @author henhen6
 * @since 2025/10/20 23:46
 */
public interface ConfigKey {
    /**
     * 是否禁止新增修改数据，演示环境专用
     */
    String NOT_ALLOW_WRITE = "NOT_ALLOW_WRITE";

    interface Console {
    }

    interface Workbench {
        /**
         * 密码错误锁定用户时间
         * 示例： 0: 今天结束  1m: 1分钟后1h: 1小时后  4d: 4天后     2w: 2周后 3M: 3个月后  5y: 5年后
         * 支持的单位：
         * m: 分钟
         * h: 小时
         * d: 天
         * w: 周
         * M: 月
         * y: 年
         *
         */
        String PASSWORD_ERROR_LOCK_USER_TIME = "PASSWORD_ERROR_LOCK_USER_TIME";

        /**
         * 密码错误几次后锁定账号
         *
         */
        String PASSWORD_ERROR_LOCK_ACCOUNT = "PASSWORD_ERROR_LOCK_ACCOUNT";
        /**
         * 密码有效期
         * 示例：  1m: 1分钟后1h: 1小时后  4d: 4天后     2w: 2周后 3M: 3个月后  5y: 5年后
         * 支持的单位：
         * m: 分钟
         * h: 小时
         * d: 天
         * w: 周
         * M: 月
         * y: 年
         */
        String PASSWORD_EXPIRE_TIME = "PASSWORD_EXPIRE_TIME";
        /**
         * 是否密码过期后不允许登录
         */
        String PASSWORD_EXPIRE_NOT_ALLOW_LOGIN = "PASSWORD_EXPIRE_NOT_ALLOW_LOGIN";

    }

    interface Open {
        /**
         * Torna服务器地址
         * 这个服务需要单独启动
         */
        String TORNA_SERVER_ADDR = "TornaServerAddr";
        /**
         * 开放平台生产地址
         */
        String OPEN_PROD_URL = "OpenProdUrl";
        /**
         * 开放平台测试地址
         */
        String OPEN_SANDBOX_URL = "OpenSandboxUrl";
        /**
         * 默认 Lower-Client-Token有效期（秒）
         */
        String APP_LOWER_CLIENT_TOKEN_TIMEOUT = "AppLowerClientTokenTimeout";
        /**
         * 默认 Client-Token 有效期（秒）
         */
        String APP_CLIENT_TOKEN_TIMEOUT = "AppClientTokenTimeout";
        /**
         * 默认Refresh-Token有效期，单位（秒）
         */
        String APP_REFRESH_TOKEN_TIMEOUT = "AppRefreshTokenTimeout";
        /**
         * 默认 Access-Token 有效期，单位（秒）
         */
        String APP_ACCESS_TOKEN_TIMEOUT = "AppAccessTokenTimeout";
        /**
         * Refresh-Token 刷新策略
         * true=每次构建新 RefreshToken，false=每次延用旧RefreshToken
         */
        String APP_NEW_REFRESH = "AppNewRefresh";
    }

}
