package top.mddata.common.database.config;

import top.mddata.base.db.properties.DatabaseProperties;
import top.mddata.base.mybatisflex.config.MyMybatisFlexConfiguration;
import com.mybatisflex.spring.boot.MybatisFlexProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

/**
 * Mybatis 常用重用拦截器
 * <p>
 *
 * @author henhen6
 * @since 2018/10/24
 */
@Configuration
@Slf4j
@EnableConfigurationProperties({DatabaseProperties.class})
@MapperScan(basePackages = "top.mddata", annotationClass = Repository.class)
public class MybatisFlexConfiguration extends MyMybatisFlexConfiguration {
    public MybatisFlexConfiguration(final DatabaseProperties databaseProperties, MybatisFlexProperties mybatisFlexProperties) {
        super(databaseProperties, mybatisFlexProperties);
    }

}
