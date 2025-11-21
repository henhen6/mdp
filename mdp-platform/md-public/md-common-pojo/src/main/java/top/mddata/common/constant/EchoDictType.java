package top.mddata.common.constant;

/**
 * Echo注解中dictType的常量
 * <p>
 * 存放系统中常用的类型
 * <p>
 * 本类中的 @md.generator auto insert 请勿删除
 *
 * @author henhen6
 * @since 2019/07/26
 */
public interface EchoDictType {
    // @md.generator auto insert EchoDictType

    /**
     * 全局字典类型
     */
    interface Global {
        // @md.generator auto insert Global

        /**
         * 行政级别
         * [10-国家 20-省份/直辖市 30-地市 40-区县 50-乡镇]
         */
        String AREA_LEVEL = "GLOBAL_AREA_LEVEL";
        /**
         * 民族
         * [01-汉族 02-...]
         */
        String NATION = "GLOBAL_NATION";
        /**
         * 学历
         * [01-小学 02-中学 03-高中 04-专科 05-本科 06-硕士 07-博士 08-博士后 99-其他]
         */
        String EDUCATION = "GLOBAL_EDUCATION";
        /**
         * 性别
         */
        String SEX = "Sex";
        /**
         * 数据类型
         *  [1-字符串 2-整型 3-布尔]
         */
        String DATA_TYPE = "DataTypeEnum";
    }


    /**
     * 后台服务
     */
    interface Console {
        // @md.generator auto insert System

        /**
         * 数据范围 [01-全部 02-本单位及子级 03-本单位 04-本部门 05-本部门及子级 06-个人 07-自定义]
         */
        String DATA_SCOPE = "DataTypeEnum";
        /**
         * 菜单类型 [10-目录  20-菜单 30-内链 40-外链]
         */
        String MENU_TYPE = "MenuTypeEnum";
        /**
         * 机构类型 [10-公司 20-部门]
         */
        String ORG_TYPE = "OrgTypeEnum";
    }
}
