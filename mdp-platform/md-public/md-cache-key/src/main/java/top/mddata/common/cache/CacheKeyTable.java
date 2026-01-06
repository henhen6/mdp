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


    interface Console {
        /**
         * 字典项
         */
        String DICT_ITEM = "dict_item";
        /**
         * 系统参数
         */
        String PARAM = "param";

        /**
         * 用户拥有那些组织
         */
        String USER_ORG = "user_org";

        /**
         * 组织
         */
        String ORG = "org";
        /** 角色拥有的资源 */
        String ROLE_RESOURCE = "role_resource";
    }

    interface Workbench {

        /**
         * 用户
         */
        String USER = "user";
    }
    interface Open {
        /**
         * 应用
         */
        String APP = "app";
        /**
         * 应用拥有的接口
         */
        String APP_API = "app_api";
        /**
         * 应用秘钥
         */
        String APP_KEYS = "app_keys";
        /**
         * 接口
         */
        String API = "api";
        /**
         * 文档
         */
        String DOC_INFO = "doc_info";
    }
}
