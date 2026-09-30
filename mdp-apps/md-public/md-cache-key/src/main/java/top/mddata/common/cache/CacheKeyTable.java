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
    /**
     * 忘记密码 前缀
     * 完整key: forget_pwd:{key} -> str
     */
    String FORGET_PWD = "forget_pwd";


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
        /** 角色 × 菜单 数据范围授权 */
        String ROLE_DATA_SCOPE = "role_data_scope";
        /** 菜单数据权限开关 */
        String RESOURCE_MENU = "resource_menu";
        /** 全量已配置接口 */
        String RESOURCE_API_ALL = "resource_api_all";
        /** 用户接口放行集 */
        String USER_RESOURCE_API = "user_resource_api";
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
        /**
         * 访问令牌
         * token -> appId
         */
        String ACCESS_TOKEN = "access_token";
    }
}
