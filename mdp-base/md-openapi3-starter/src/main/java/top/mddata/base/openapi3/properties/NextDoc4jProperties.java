package top.mddata.base.openapi3.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.mddata.base.constant.Constants;

import static top.mddata.base.openapi3.properties.NextDoc4jProperties.PREFIX;

/**
 * NextDoc4j 在线文档属性配置
 * 必须配置 prefix ，才能有提示
 *
 * @author henhen6
 * @since 2018/11/18 9:17
 */
@Data
@ConfigurationProperties(prefix = PREFIX)
public class NextDoc4jProperties {
    public static final String PREFIX = Constants.PROJECT_PREFIX + ".nextdoc4j";

    /**
     * 标题
     **/
    private String title = "在线文档";
    /**
     * 描述
     **/
    private String description = "md-cloud 在线文档";
    /**
     * 版本
     **/
    private String version = "1.0";
    /**
     * 许可证
     **/
    private String license = "";
    /**
     * 许可证URL
     **/
    private String licenseUrl = "";
    /**
     * 服务条款URL
     **/
    private String termsOfServiceUrl = "";

    private Contact contact = new Contact();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Contact {
        /**
         * 联系人
         **/
        private String name = "";
        /**
         * 联系人url
         **/
        private String url = "";
        /**
         * 联系人email
         **/
        private String email = "";
    }

}
