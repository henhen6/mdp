package top.mddata.base.mybatisflex.config;

import com.mybatisflex.spring.boot.MybatisFlexProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import top.mddata.base.db.properties.DatabaseProperties;
import top.mddata.base.uid.dao.WorkerNodeDao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 自动配置自包含性验证：仅引入 md-db-mybatis-flex（无 md-common-config）时，
 * {@link DatabaseProperties} 必须由 {@link MdMybatisFlexConfiguration} 自身激活。
 *
 * <p>回归背景：inner-gateway-server 启动报 "Parameter 0 of constructor in
 * MdMybatisFlexConfiguration required a bean of type DatabaseProperties that could not be found"，
 * 根因是属性激活绑定在 md-common-config，自动配置未自包含。</p>
 *
 * @author henhen6
 * @since 2026-10-05
 */
class MdMybatisFlexConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MdMybatisFlexConfiguration.class))
            .withBean(MybatisFlexProperties.class)
            .withBean(WorkerNodeDao.class, () -> mock(WorkerNodeDao.class));

    @Test
    void 无外部激活时_DatabaseProperties由自动配置自包含激活() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(DatabaseProperties.class);
            assertThat(context).hasSingleBean(MdMybatisFlexConfiguration.class);
        });
    }
}
