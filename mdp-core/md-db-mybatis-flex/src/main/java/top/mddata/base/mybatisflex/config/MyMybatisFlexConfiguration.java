package top.mddata.base.mybatisflex.config;


import top.mddata.base.base.entity.BaseEntity;
import top.mddata.base.db.config.DbConfiguration;
import top.mddata.base.db.properties.DatabaseProperties;
import top.mddata.base.mybatisflex.keygen.UidKeyGenerator;
import top.mddata.base.mybatisflex.listener.DefaultInsertListener;
import top.mddata.base.mybatisflex.listener.DefaultUpdateListener;
import top.mddata.base.mybatisflex.logicdelete.TimeStampDelByLogicDeleteProcessor;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.CountableMessageCollector;
import com.mybatisflex.core.audit.ScheduledMessageCollector;
import com.mybatisflex.core.dialect.DbType;
import com.mybatisflex.core.keygen.KeyGeneratorFactory;
import com.mybatisflex.core.logicdelete.LogicDeleteManager;
import com.mybatisflex.core.logicdelete.impl.BooleanLogicDeleteProcessor;
import com.mybatisflex.core.logicdelete.impl.DateTimeLogicDeleteProcessor;
import com.mybatisflex.core.logicdelete.impl.IntegerLogicDeleteProcessor;
import com.mybatisflex.core.logicdelete.impl.PrimaryKeyLogicDeleteProcessor;
import com.mybatisflex.core.logicdelete.impl.TimeStampLogicDeleteProcessor;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import com.mybatisflex.spring.boot.MybatisFlexProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

/**
 * Mybatis flex 常用重用拦截器
 *
 * @author henhen6
 * @since 2018/10/24
 */
@Slf4j
public abstract class MyMybatisFlexConfiguration extends DbConfiguration implements MyBatisFlexCustomizer {
    protected final MybatisFlexProperties mybatisFlexProperties;

    public MyMybatisFlexConfiguration(final DatabaseProperties databaseProperties, MybatisFlexProperties mybatisFlexProperties) {
        super(databaseProperties);
        this.mybatisFlexProperties = mybatisFlexProperties;
    }

    /** UID注册 */
    private static void uid(FlexGlobalConfig globalConfig) {
        FlexGlobalConfig.KeyConfig keyConfig = globalConfig.getKeyConfig();
        if (keyConfig != null && StrUtil.isNotEmpty(keyConfig.getValue())) {
            KeyGeneratorFactory.register(keyConfig.getValue(), new UidKeyGenerator());
        }
    }

    private static String formatSql(String sql) {
        return sql.replaceAll("\\s+", " ").replace("\\r", " ").replace("\\n", " ");
    }

    /**
     * 数据库配置
     *
     * @return 配置
     */
    @Bean
    public DatabaseIdProvider getDatabaseIdProvider() {
        DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        properties.setProperty("Oracle", DbType.ORACLE.getName());
        properties.setProperty("MySQL", DbType.MYSQL.getName());
        properties.setProperty("Microsoft SQL Server", DbType.SQLSERVER.getName());
        databaseIdProvider.setProperties(properties);
        return databaseIdProvider;
    }

    @Override
    public void customize(FlexGlobalConfig globalConfig) {
        audit();

        uid(globalConfig);

        logicDelete();

        // 全局监听器
        globalConfig.registerInsertListener(new DefaultInsertListener(), BaseEntity.class);
        globalConfig.registerUpdateListener(new DefaultUpdateListener(), BaseEntity.class);

    }

    /** 逻辑删除处理器 */
    public void logicDelete() {
        DatabaseProperties.Flex flex = databaseProperties.getFlex();

        switch (flex.getLogicDeleteProcessor()) {
            case INTEGER_LOGIC_DELETE_PROCESSOR -> LogicDeleteManager.setProcessor(new IntegerLogicDeleteProcessor());
            case BOOLEAN_LOGIC_DELETE_PROCESSOR -> LogicDeleteManager.setProcessor(new BooleanLogicDeleteProcessor());
            case DATE_TIME_LOGIC_DELETE_PROCESSOR ->
                    LogicDeleteManager.setProcessor(new DateTimeLogicDeleteProcessor());
            case TIME_STAMP_LOGIC_DELETE_PROCESSOR ->
                    LogicDeleteManager.setProcessor(new TimeStampLogicDeleteProcessor());
            case PRIMARY_KEY_LOGIC_DELETE_PROCESSOR ->
                    LogicDeleteManager.setProcessor(new PrimaryKeyLogicDeleteProcessor());
            default ->
                    LogicDeleteManager.setProcessor(new TimeStampDelByLogicDeleteProcessor(flex.getDeletedByColumn()));
        }
    }

    /** SQL审计 */
    public void audit() {
        DatabaseProperties.Flex flex = databaseProperties.getFlex();

        //开启审计功能
        AuditManager.setAuditEnable(flex.getAudit());

        switch (flex.getAuditCollector()) {
            case CONSOLE -> AuditManager.setMessageCollector(new ConsoleMessageCollector());
            case COUNTABLE -> AuditManager.setMessageCollector(new CountableMessageCollector());
            case SCHEDULED -> AuditManager.setMessageCollector(new ScheduledMessageCollector());
            default -> AuditManager.setMessageCollector(auditMessage ->
                    log.info("""
                                    SQL:
                                    数据源: {} | 查询数据量: {} 条 | 消耗时间: {} ms
                                    {}""",
                            auditMessage.getDsName(),
                            auditMessage.getQueryCount(),
                            auditMessage.getElapsedTime(),
                            formatSql(auditMessage.getFullSql())
                    ));
        }
    }

}
