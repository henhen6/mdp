package top.mddata.common.cache;

/**
 *
 * @author henhen6
 * @since 2025/7/9 12:35
 */
public interface CacheKeyTable {
    /**
     * 验证码 前缀
     * 完整key: captcha:{key} -> str
     */
    String CAPTCHA = "captcha";


    interface Admin {
        /**
         * 字典项
         */
        String SYS_DICT_ITEM = "sys_dict_item";
        /**
         * 系统参数
         */
        String SYS_PARAM = "sys_param";

        /**
         * 用户拥有那些组织
         */
        String USER_ORG = "user_org";

        /**
         * 组织
         */
        String SYS_ORG = "sys_org";
    }

    interface Center {

        /**
         * 用户
         */
        String SSO_USER = "sys_user";
    }
}
