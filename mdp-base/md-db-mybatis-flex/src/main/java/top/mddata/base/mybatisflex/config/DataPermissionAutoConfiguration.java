package top.mddata.base.mybatisflex.config;

import com.mybatisflex.core.dialect.DbType;
import com.mybatisflex.core.dialect.DialectFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import top.mddata.base.db.properties.DatabaseProperties;
import top.mddata.base.mybatisflex.datapermission.DataPermissionAspect;
import top.mddata.base.mybatisflex.datapermission.DataPermissionDialect;
import top.mddata.base.mybatisflex.datapermission.DataPermissionFilter;
import top.mddata.base.mybatisflex.datapermission.DataScopeCustomHandler;

import java.util.Map;

/**
 * 数据权限自动配置
 * <p>
 * 自动注册 DataPermissionAspect 切面 Bean，并启用 AspectJ 自动代理
 * </p>
 *
 * @author henhen
 * @since 2026年05月24日
 */
@AutoConfiguration
@EnableAspectJAutoProxy
@Slf4j
@ConditionalOnProperty(prefix = DatabaseProperties.PREFIX + ".flex", name = "dataScope", havingValue = "true", matchIfMissing = false)
public class DataPermissionAutoConfiguration {

    /**
     * 注册数据权限切面 Bean
     * <p>
     * 只有将 Aspect 注册为 Spring Bean，Spring AOP 才能激活并拦截带有 @DataPermission 注解的方法
     * </p>
     *
     * @return DataPermissionAspect 实例
     */
    @Bean
    public DataPermissionAspect dataPermissionAspect() {
        return new DataPermissionAspect();
    }

    @Resource
    private DataPermissionFilter dataPermissionFilter;

    /**
     * 聚合容器中所有自定义数据范围处理器，key 为 Bean 名
     * （对应角色的 data_scope_impl）
     */
    @Autowired(required = false)
    private Map<String, DataScopeCustomHandler> customHandlers;

    @PostConstruct
    public void postConstruct() {
        log.debug("MyBatis Flex 自动配置初始化完成");
        DialectFactory.registerDialect(DbType.MYSQL,
                new DataPermissionDialect(dataPermissionFilter, customHandlers));
    }

}
