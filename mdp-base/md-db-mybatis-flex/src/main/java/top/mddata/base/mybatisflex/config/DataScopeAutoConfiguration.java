package top.mddata.base.mybatisflex.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import top.mddata.base.db.properties.DatabaseProperties;
import top.mddata.base.mybatisflex.datascope.context.DataScopeAspect;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeCustomHandler;
import top.mddata.base.mybatisflex.datascope.engine.DataScopeInterceptor;
import top.mddata.base.mybatisflex.datascope.spi.DataScopeProvider;

import java.util.Map;

/**
 * 数据权限自动配置。
 *
 * <p>DataScopeInterceptor 是 MyBatis Interceptor Bean，
 * 由 MybatisFlexAutoConfiguration 自动收集注册到所有 SqlSessionFactory，
 * 无需手动挂接。</p>
 *
 * @author henhen
 * @since 2026年09月26日
 */
@AutoConfiguration
@EnableAspectJAutoProxy
@ConditionalOnProperty(prefix = DatabaseProperties.PREFIX + ".flex", name = "dataScope",
        havingValue = "true", matchIfMissing = false)
public class DataScopeAutoConfiguration {

    @Bean
    public DataScopeAspect dataScopeAspect() {
        return new DataScopeAspect();
    }

    @Bean
    public DataScopeInterceptor dataScopeInterceptor(
            DataScopeProvider dataScopeProvider,
            Map<String, DataScopeCustomHandler> customHandlers) {
        return new DataScopeInterceptor(dataScopeProvider, customHandlers);
    }
}
