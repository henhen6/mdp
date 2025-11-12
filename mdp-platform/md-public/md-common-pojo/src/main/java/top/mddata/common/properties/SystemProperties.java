package top.mddata.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import top.mddata.base.constant.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录配置
 *
 * @author henhen6
 * @date 2025/6/30 7:57 下午
 */
@Setter
@Getter
@RefreshScope
@ConfigurationProperties(prefix = SystemProperties.PREFIX)
public class SystemProperties {
    public static final String PREFIX = Constants.PROJECT_PREFIX + ".system";

    private Mode mode;
    /**
     * 登录时 是否验证密码， 开发环境使用
     */
    private Boolean verifyPassword = true;
    /**
     * 登录时 是否验证验证码， 开发环境使用
     */
    private Boolean verifyCaptcha = true;
    /**
     * 默认用户密码
     */
    private String defPwd = "123456";
    /**
     * 记录 cloud 或 boot 项目所有方法的日志
     */
    private Boolean recordLog = false;
    /**
     * 记录 cloud 或 boot 项目所有方法的入参
     */
    private Boolean recordArgs = true;
    /**
     * 记录 cloud 或 boot 项目所有方法的返回值
     */
    private Boolean recordResult = true;
    /**
     * 缓存Key前缀
     *
     */
    private String cachePrefix;
    /** oauth 服务扫描枚举类的包路径 */
    private String enumPackage;
    /**
     * 是否禁止写入
     */
    private Boolean notAllowWrite = false;
    /**
     * 禁止写入名单
     */
    private Map<String, List<String>> notAllowWriteList = new HashMap<>();


    public enum Mode {
        /***
         * 微服务架构
         */
        cloud,
        /**
         * 单体架构
         */
        boot;
    }
}