package top.mddata.common.constant;

/**
 * 上传附件的业务类型
 * <p>
 * 一个表，拥有多个附件时，为每个业务类型定义一个业务类型
 *
 * @author henhen6
 * @since 2025/10/20 23:46
 */
public interface FileObjectType {

    /**
     * 文件临时对象类型
     */
    String TEMP_OBJECT_TYPE = "temp";

    interface Admin {
        /**
         * 用户头像
         */
        String USER_AVATAR = "USER_AVATAR";

    }

    interface Center {
    }

    interface Open {

        /**
         * 应用LOGO
         */
        String APP_LOGO = "APP_LOGO";
        /**
         * 应用申请LOGO
         */
        String APP_APPLY_LOGO = "APP_APPLY_LOGO";
        /**
         * 应用申请资质文件
         */
        String APP_APPLY_CREDENTIAL_FILE = "APP_APPLY_CREDENTIAL_FILE";
    }
}
