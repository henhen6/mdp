package top.mddata.common.constant;

/**
 * 事件类型 编码
 * @author henhen
 * @since 2026/1/13 15:54
 */
public interface EventTypeCode {
    interface Console {
        String ORG_ADD = "org:add";
        String ORG_EDIT = "org:edit";
        String ORG_DELETE = "org:delete";
        String USER_ADD = "user:add";
        String USER_EDIT = "user:edit";
        String USER_DELETE = "user:delete";
    }

    interface Workbench {
        /**
         * 用户资料变更（编辑资料、手机号、邮箱、密码等自助修改）
         * <p>
         * 码值与 Console.USER_EDIT 一致是有意的——订阅方关心的是「用户数据变了」这一事实，
         * 不关心变更来源；此处修改只是把 workbench 侧的调用归位到正确命名空间。
         */
        String USER_EDIT = Console.USER_EDIT;
    }

    interface Open {

    }
}
