package top.mddata.console.system.properties;


import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import top.mddata.base.constant.Constants;
import top.mddata.base.exception.BizException;

import java.util.HashSet;
import java.util.Set;


/**
 * 文件存储配置
 * @author henhen6
 */
@Setter
@Getter
@RefreshScope
@ConfigurationProperties(prefix = FileProperties.PREFIX)
public class FileProperties {
    public static final String PREFIX = Constants.PROJECT_PREFIX + ".file";
    /**
     * 调用接口删除附件时，是否删除文件系统中的文件
     */
    private Boolean delFile = false;
    /**
     * 公开桶
     * <p>
     * 配置后获取连接时改桶下的 url = urlPrefix + path
     * 使用场景：
     * 1. 富文本编辑器
     * 2. 需要url永久访问的场景
     */
    private Set<String> publicBucket = new HashSet<>();
    /**
     * 支持的文件后缀
     */
    private String suffix;

    public boolean validSuffix(String name) {
        if (StrUtil.isEmpty(suffix)) {
            throw BizException.wrap("请配置文件名后缀白名单");
        }
        if (StrUtil.isEmpty(name)) {
            throw BizException.wrap("文件名不能为空");
        }
        return StrUtil.split(suffix, ",").stream().anyMatch(name::endsWith);
    }

}
