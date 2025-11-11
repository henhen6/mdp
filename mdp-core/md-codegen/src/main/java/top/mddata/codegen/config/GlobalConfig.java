/*
 *  Copyright (c) 2022-2024, Mybatis-Flex (fuhai999@gmail.com).
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package top.mddata.codegen.config;

import top.mddata.codegen.entity.Table;
import top.mddata.codegen.template.ITemplate;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * 代码生成全局配置类。
 *
 * @author Michael Yang
 * @author 王帅
 * @since 2023-05-15
 */
@SuppressWarnings("unused")
public class GlobalConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 5033600623041298000L;

    // === 必须配置 ===

    private final JavadocConfig javadocConfig;
    private final PackageConfig packageConfig;
    private final StrategyConfig strategyConfig;
    private final TemplateConfig templateConfig;

    // === 可选配置 ===

    private FrontConfig frontConfig;
    private EntityConfig entityConfig;
    private VoConfig voConfig;
    private DtoConfig dtoConfig;
    private QueryConfig queryConfig;
    private MapperConfig mapperConfig;
    private ServiceConfig serviceConfig;
    private ServiceImplConfig serviceImplConfig;
    private ControllerConfig controllerConfig;
    private TableDefConfig tableDefConfig;
    private MapperXmlConfig mapperXmlConfig;

    // === 其他自定义配置 ===
    private Map<String, Object> customConfig = new HashMap<>();

    // === 是否启用生成 ===

    /**
     * 启用前端 单表表格 样式
     */
    private Boolean frontTableGenerateEnable = false;
    /**
     * 启用前端 单表树结构 样式
     */
    private Boolean frontTreeGenerateEnable = false;
    private Boolean entityGenerateEnable = false;
    private Boolean voGenerateEnable = false;
    private Boolean dtoGenerateEnable = false;
    private Boolean queryGenerateEnable = false;
    private Boolean mapperGenerateEnable = false;
    private Boolean serviceGenerateEnable = false;
    private Boolean serviceImplGenerateEnable = false;
    private Boolean controllerGenerateEnable = false;
    private Boolean tableDefGenerateEnable = false;
    private Boolean mapperXmlGenerateEnable = false;
    private Boolean packageInfoGenerateEnable = false;


    public GlobalConfig() {
        this.javadocConfig = new JavadocConfig();
        this.packageConfig = new PackageConfig();
        this.strategyConfig = new StrategyConfig();
        this.templateConfig = new TemplateConfig();
    }

    // === 分类配置 ===

    public JavadocConfig getJavadocConfig() {
        return javadocConfig;
    }

    public PackageConfig getPackageConfig() {
        return packageConfig;
    }

    public StrategyConfig getStrategyConfig() {
        return strategyConfig;
    }

    public TemplateConfig getTemplateConfig() {
        return templateConfig;
    }

    public EntityConfig getEntityConfig() {
        if (entityConfig == null) {
            entityConfig = new EntityConfig();
        }
        return entityConfig;
    }

    public FrontConfig getFrontConfig() {
        if (frontConfig == null) {
            frontConfig = new FrontConfig();
        }
        return frontConfig;
    }

    public VoConfig getVoConfig() {
        if (voConfig == null) {
            voConfig = new VoConfig();
        }
        return voConfig;
    }

    public DtoConfig getDtoConfig() {
        if (dtoConfig == null) {
            dtoConfig = new DtoConfig();
        }
        return dtoConfig;
    }

    public QueryConfig getQueryConfig() {
        if (queryConfig == null) {
            queryConfig = new QueryConfig();
        }
        return queryConfig;
    }

    public MapperConfig getMapperConfig() {
        if (mapperConfig == null) {
            mapperConfig = new MapperConfig();
        }
        return mapperConfig;
    }

    public ServiceConfig getServiceConfig() {
        if (serviceConfig == null) {
            serviceConfig = new ServiceConfig();
        }
        return serviceConfig;
    }

    public ServiceImplConfig getServiceImplConfig() {
        if (serviceImplConfig == null) {
            serviceImplConfig = new ServiceImplConfig();
        }
        return serviceImplConfig;
    }

    public ControllerConfig getControllerConfig() {
        if (controllerConfig == null) {
            controllerConfig = new ControllerConfig();
        }
        return controllerConfig;
    }

    public TableDefConfig getTableDefConfig() {
        if (tableDefConfig == null) {
            tableDefConfig = new TableDefConfig();
        }
        return tableDefConfig;
    }

    public MapperXmlConfig getMapperXmlConfig() {
        if (mapperXmlConfig == null) {
            mapperXmlConfig = new MapperXmlConfig();
        }
        return mapperXmlConfig;
    }

    // === 启用配置 ===
    public FrontConfig enableFrontTable() {
        frontTableGenerateEnable = true;
        return getFrontConfig();
    }

    public FrontConfig enableFrontTree() {
        frontTreeGenerateEnable = true;
        return getFrontConfig();
    }

    public EntityConfig enableEntity() {
        entityGenerateEnable = true;
        return getEntityConfig();
    }

    public VoConfig enableVo() {
        voGenerateEnable = true;
        return getVoConfig();
    }

    public DtoConfig enableDto() {
        dtoGenerateEnable = true;
        return getDtoConfig();
    }

    public QueryConfig enableQuery() {
        queryGenerateEnable = true;
        return getQueryConfig();
    }

    public MapperConfig enableMapper() {
        mapperGenerateEnable = true;
        return getMapperConfig();
    }

    public ServiceConfig enableService() {
        serviceGenerateEnable = true;
        return getServiceConfig();
    }

    public ServiceImplConfig enableServiceImpl() {
        serviceImplGenerateEnable = true;
        return getServiceImplConfig();
    }

    public ControllerConfig enableController() {
        controllerGenerateEnable = true;
        return getControllerConfig();
    }

    public TableDefConfig enableTableDef() {
        tableDefGenerateEnable = true;
        return getTableDefConfig();
    }

    public MapperXmlConfig enableMapperXml() {
        mapperXmlGenerateEnable = true;
        return getMapperXmlConfig();
    }

    public void enablePackageInfo() {
        packageInfoGenerateEnable = true;
    }

    // === 禁用配置 ===

    public void disableFrontTable() {
        frontTableGenerateEnable = false;
    }

    public void disableFrontTree() {
        frontTreeGenerateEnable = false;
    }

    public void disableBackend() {
        entityGenerateEnable = false;
        voGenerateEnable = false;
        dtoGenerateEnable = false;
        queryGenerateEnable = false;
        mapperGenerateEnable = false;
        serviceGenerateEnable = false;
        serviceImplGenerateEnable = false;
        controllerGenerateEnable = false;
        tableDefGenerateEnable = false;
        mapperXmlGenerateEnable = false;
        packageInfoGenerateEnable = false;
    }

    public void disableEntity() {
        entityGenerateEnable = false;
    }

    public void disableVo() {
        voGenerateEnable = false;
    }

    public void disableDto() {
        dtoGenerateEnable = false;
    }

    public void disableQuery() {
        queryGenerateEnable = false;
    }

    public void disableMapper() {
        mapperGenerateEnable = false;
    }

    public void disableService() {
        serviceGenerateEnable = false;
    }

    public void disableServiceImpl() {
        serviceImplGenerateEnable = false;
    }

    public void disableController() {
        controllerGenerateEnable = false;
    }

    public void disableTableDef() {
        tableDefGenerateEnable = false;
    }

    public void disableMapperXml() {
        mapperXmlGenerateEnable = false;
    }

    public void disablePackageInfo() {
        packageInfoGenerateEnable = false;
    }


    // === 自定义配置 ===

    public Object getCustomConfig(String key) {
        return customConfig.get(key);
    }

    public void setCustomConfig(String key, Object value) {
        customConfig.put(key, value);
    }

    public Map<String, Object> getCustomConfig() {
        return customConfig;
    }

    public void setCustomConfig(Map<String, Object> customConfig) {
        this.customConfig = customConfig;
    }
    // === 分项配置 ===

    /**
     * @see JavadocConfig#getAuthor()
     */
    public String getAuthor() {
        return getJavadocConfig().getAuthor();
    }

    /**
     * @see JavadocConfig#setAuthor(String)
     */
    public void setAuthor(String author) {
        getJavadocConfig().setAuthor(author);
    }

    /**
     * @see JavadocConfig#getSince()
     */
    public String getSince() {
        return getJavadocConfig().getSince();
    }

    /**
     * @see JavadocConfig#setSince(String)
     */
    public void setSince(String since) {
        getJavadocConfig().setSince(since);
    }

    /**
     * @see JavadocConfig#setSince(Supplier)
     */
    public void setSince(Supplier<String> since) {
        getJavadocConfig().setSince(since);
    }

    /**
     * @see JavadocConfig#getTableCommentFormat()
     */
    public Function<String, String> getTableCommentFormat() {
        return getJavadocConfig().getTableCommentFormat();
    }

    /**
     * @see JavadocConfig#setTableCommentFormat(UnaryOperator)
     */
    public void setTableCommentFormat(UnaryOperator<String> tableCommentFormat) {
        getJavadocConfig().setTableCommentFormat(tableCommentFormat);
    }

    /**
     * @see JavadocConfig#getColumnCommentFormat()
     */
    public Function<String, String> getColumnCommentFormat() {
        return getJavadocConfig().getColumnCommentFormat();
    }

    /**
     * @see JavadocConfig#setColumnCommentFormat(UnaryOperator)
     */
    public void setColumnCommentFormat(UnaryOperator<String> columnCommentFormat) {
        getJavadocConfig().setColumnCommentFormat(columnCommentFormat);
    }

    /**
     * @see JavadocConfig#getEntityPackage()
     */
    public String getEntityPackageComment() {
        return getJavadocConfig().getEntityPackage();
    }

    /**
     * @see JavadocConfig#setEntityPackage(String)
     */
    public void setEntityPackageComment(String entityPackageComment) {
        getJavadocConfig().setEntityPackage(entityPackageComment);
    }

    /**
     * @see JavadocConfig#getVoPackage()
     */
    public String getVoPackageComment() {
        return getJavadocConfig().getVoPackage();
    }

    /**
     * @see JavadocConfig#setVoPackage(String)
     */
    public void setVoPackageComment(String voPackageComment) {
        getJavadocConfig().setVoPackage(voPackageComment);
    }


    /**
     * @see JavadocConfig#getQueryPackage()
     */
    public String getQueryPackageComment() {
        return getJavadocConfig().getQueryPackage();
    }

    /**
     * @see JavadocConfig#setQueryPackage(String)
     */
    public void setQueryPackageComment(String queryPackageComment) {
        getJavadocConfig().setQueryPackage(queryPackageComment);
    }

    /**
     * @see JavadocConfig#getDtoPackage()
     */
    public String getDtoPackageComment() {
        return getJavadocConfig().getDtoPackage();
    }

    /**
     * @see JavadocConfig#setVoPackage(String)
     */
    public void setDtoPackageComment(String dtoPackageComment) {
        getJavadocConfig().setDtoPackage(dtoPackageComment);
    }

    /**
     * @see JavadocConfig#getMapperPackage()
     */
    public String getMapperPackageComment() {
        return getJavadocConfig().getMapperPackage();
    }

    /**
     * @see JavadocConfig#setMapperPackage(String)
     */
    public void setMapperPackageComment(String mapperPackageComment) {
        getJavadocConfig().setMapperPackage(mapperPackageComment);
    }

    /**
     * @see JavadocConfig#getServicePackage()
     */
    public String getServicePackageComment() {
        return getJavadocConfig().getServicePackage();
    }

    /**
     * @see JavadocConfig#setServicePackage(String)
     */
    public void setServicePackageComment(String servicePackageComment) {
        getJavadocConfig().setServicePackage(servicePackageComment);
    }

    /**
     * @see JavadocConfig#getServiceImplPackage()
     */
    public String getServiceImplPackageComment() {
        return getJavadocConfig().getServiceImplPackage();
    }

    /**
     * @see JavadocConfig#setServiceImplPackage(String)
     */
    public void setServiceImplPackageComment(String serviceImplPackageComment) {
        getJavadocConfig().setServiceImplPackage(serviceImplPackageComment);
    }

    /**
     * @see JavadocConfig#getControllerPackage()
     */
    public String getControllerPackageComment() {
        return getJavadocConfig().getControllerPackage();
    }

    /**
     * @see JavadocConfig#setControllerPackage(String)
     */
    public void setControllerPackageComment(String controllerPackageComment) {
        getJavadocConfig().setControllerPackage(controllerPackageComment);
    }

    /**
     * @see JavadocConfig#getTableDefPackage()
     */
    public String getTableDefPackageComment() {
        return getJavadocConfig().getTableDefPackage();
    }

    /**
     * @see JavadocConfig#setTableDefPackage(String)
     */
    public void setTableDefPackageComment(String tableDefPackageComment) {
        getJavadocConfig().setTableDefPackage(tableDefPackageComment);
    }

    /**
     * @see PackageConfig#getSourceDir()
     */
    public String getSourceDir() {
        return getPackageConfig().getSourceDir();
    }

    /**
     * @see PackageConfig#setSourceDir(String)
     */
    public void setSourceDir(String sourceDir) {
        getPackageConfig().setSourceDir(sourceDir);
    }

    public void setFrontSourceDir(String frontSourceDir) {
        getPackageConfig().setFrontSourceDir(frontSourceDir);
    }

    /**
     * @see PackageConfig#getBasePackage()
     */
    public String getBasePackage() {
        return getPackageConfig().getBasePackage();
    }

    /**
     * @see PackageConfig#setBasePackage(String)
     */
    public void setBasePackage(String basePackage) {
        getPackageConfig().setBasePackage(basePackage);
    }

    /**
     * @see PackageConfig#getEntityPackage()
     */
    public String getEntityPackage() {
        return getPackageConfig().getEntityPackage();
    }

    /**
     * @see PackageConfig#setEntityPackage(String)
     */
    public void setEntityPackage(String entityPackage) {
        getPackageConfig().setEntityPackage(entityPackage);
    }

    /**
     * @see PackageConfig#getVoPackage()
     */
    public String getVoPackage() {
        return getPackageConfig().getVoPackage();
    }

    /**
     * @see PackageConfig#setVoPackage(String)
     */
    public void setVoPackage(String voPackage) {
        getPackageConfig().setVoPackage(voPackage);
    }

    /**
     * @see PackageConfig#getQueryPackage()
     */
    public String getQueryPackage() {
        return getPackageConfig().getQueryPackage();
    }

    /**
     * @see PackageConfig#setQueryPackage(String)
     */
    public void setQueryPackage(String queryPackage) {
        getPackageConfig().setQueryPackage(queryPackage);
    }

    /**
     * @see PackageConfig#getDtoPackage()
     */
    public String getDtoPackage() {
        return getPackageConfig().getDtoPackage();
    }

    /**
     * @see PackageConfig#setDtoPackage(String)
     */
    public void setDtoPackage(String dtoPackage) {
        getPackageConfig().setDtoPackage(dtoPackage);
    }

    /**
     * @see PackageConfig#getMapperPackage()
     */
    public String getMapperPackage() {
        return getPackageConfig().getMapperPackage();
    }

    /**
     * @see PackageConfig#setMapperPackage(String)
     */
    public void setMapperPackage(String mapperPackage) {
        getPackageConfig().setMapperPackage(mapperPackage);
    }

    /**
     * @see PackageConfig#getServicePackage()
     */
    public String getServicePackage() {
        return getPackageConfig().getServicePackage();
    }

    /**
     * @see PackageConfig#setServicePackage(String)
     */
    public void setServicePackage(String servicePackage) {
        getPackageConfig().setServicePackage(servicePackage);
    }

    /**
     * @see PackageConfig#getServiceImplPackage()
     */
    public String getServiceImplPackage() {
        return getPackageConfig().getServiceImplPackage();
    }

    /**
     * @see PackageConfig#setServiceImplPackage(String)
     */
    public void setServiceImplPackage(String serviceImplPackage) {
        getPackageConfig().setServiceImplPackage(serviceImplPackage);
    }

    /**
     * @see PackageConfig#getControllerPackage()
     */
    public String getControllerPackage() {
        return getPackageConfig().getControllerPackage();
    }

    /**
     * @see PackageConfig#setControllerPackage(String)
     */
    public void setControllerPackage(String controllerPackage) {
        getPackageConfig().setControllerPackage(controllerPackage);
    }

    /**
     * @see PackageConfig#getTableDefPackage()
     */
    public String getTableDefPackage() {
        return getPackageConfig().getTableDefPackage();
    }

    /**
     * @see PackageConfig#setTableDefPackage(String)
     */
    public void setTableDefPackage(String tableDefPackage) {
        getPackageConfig().setTableDefPackage(tableDefPackage);
    }

    /**
     * @see PackageConfig#getMapperXmlPath()
     */
    public String getMapperXmlPath() {
        return getPackageConfig().getMapperXmlPath();
    }

    /**
     * @see PackageConfig#setMapperXmlPath(String)
     */
    public void setMapperXmlPath(String mapperXmlPath) {
        getPackageConfig().setMapperXmlPath(mapperXmlPath);
    }

    /**
     * @see StrategyConfig#getTablePrefix()
     */
    public String getTablePrefix() {
        return getStrategyConfig().getTablePrefix();
    }

    /**
     * @see StrategyConfig#setTablePrefix(String...)
     */
    public void setTablePrefix(String... tablePrefix) {
        getStrategyConfig().setTablePrefix(tablePrefix);
    }

    /**
     * @see StrategyConfig#getLogicDeleteColumn()
     */
    public String getLogicDeleteColumn() {
        return getStrategyConfig().getLogicDeleteColumn();
    }

    /**
     * @see StrategyConfig#setLogicDeleteColumn(String)
     */
    public void setLogicDeleteColumn(String logicDeleteColumn) {
        getStrategyConfig().setLogicDeleteColumn(logicDeleteColumn);
    }

    /**
     * @see StrategyConfig#getVersionColumn()
     */
    public String getVersionColumn() {
        return getStrategyConfig().getVersionColumn();
    }

    /**
     * @see StrategyConfig#setVersionColumn(String)
     */
    public void setVersionColumn(String versionColumn) {
        getStrategyConfig().setVersionColumn(versionColumn);
    }

    /**
     * @see StrategyConfig#getTableConfigMap()
     */
    public Map<String, TableConfig> getTableConfigMap() {
        return getStrategyConfig().getTableConfigMap();
    }

    /**
     * @see StrategyConfig#setTableConfigMap(Map)
     */
    public void setTableConfigMap(Map<String, TableConfig> tableConfigMap) {
        getStrategyConfig().setTableConfigMap(tableConfigMap);
    }

    /**
     * @see StrategyConfig#setTableConfig(TableConfig)
     */
    public void setTableConfig(TableConfig tableConfig) {
        getStrategyConfig().setTableConfig(tableConfig);
    }

    /**
     * @see StrategyConfig#getTableConfig(String)
     */
    public TableConfig getTableConfig(String tableName) {
        return getStrategyConfig().getTableConfig(tableName);
    }

    /**
     * @see StrategyConfig#getColumnConfigMap()
     */
    public Map<String, ColumnConfig> getColumnConfigMap() {
        return getStrategyConfig().getColumnConfigMap();
    }

    /**
     * @see StrategyConfig#setColumnConfigMap(Map)
     */
    public void setColumnConfigMap(Map<String, ColumnConfig> columnConfigMap) {
        getStrategyConfig().setColumnConfigMap(columnConfigMap);
    }

    /**
     * @see StrategyConfig#setColumnConfig(ColumnConfig)
     */
    public void setColumnConfig(ColumnConfig columnConfig) {
        getStrategyConfig().setColumnConfig(columnConfig);
    }

    /**
     * @see StrategyConfig#setColumnConfig(String, ColumnConfig)
     */
    public void setColumnConfig(String tableName, ColumnConfig columnConfig) {
        getStrategyConfig().setColumnConfig(tableName, columnConfig);
    }

    /**
     * @see StrategyConfig#getColumnConfig(String, String)
     */
    public ColumnConfig getColumnConfig(String tableName, String columnName) {
        return getStrategyConfig().getColumnConfig(tableName, columnName);
    }

    /**
     * @see StrategyConfig#getGenerateForView()
     */
    public Boolean getGenerateForView() {
        return getStrategyConfig().getGenerateForView();
    }

    /**
     * @see StrategyConfig#setGenerateForView(Boolean)
     */
    public void setGenerateForView(Boolean generateForView) {
        getStrategyConfig().setGenerateForView(generateForView);
    }


    /**
     * @see StrategyConfig#getGenerateSchema()
     */
    public String getGenerateSchema() {
        return getStrategyConfig().getGenerateSchema();
    }

    /**
     * @see StrategyConfig#setGenerateSchema(String)
     */
    public void setGenerateSchema(String generateSchema) {
        getStrategyConfig().setGenerateSchema(generateSchema);
    }


    /**
     * @see StrategyConfig#getGenerateTables()
     */
    public Set<String> getGenerateTables() {
        return getStrategyConfig().getGenerateTables();
    }

    /**
     * @see StrategyConfig#setGenerateTables(Set)
     */
    public void setGenerateTables(Set<String> generateTables) {
        getStrategyConfig().setGenerateTables(generateTables);
    }

    /**
     * @see StrategyConfig#setGenerateTable(String...)
     */
    public void setGenerateTable(String... tables) {
        getStrategyConfig().setGenerateTable(tables);
    }

    /**
     * @see StrategyConfig#getUnGenerateTables()
     */
    public Set<String> getUnGenerateTables() {
        return getStrategyConfig().getUnGenerateTables();
    }

    /**
     * @see StrategyConfig#setUnGenerateTables(Set)
     */
    public void setUnGenerateTables(Set<String> unGenerateTables) {
        getStrategyConfig().setUnGenerateTables(unGenerateTables);
    }

    /**
     * @see StrategyConfig#setUnGenerateTable(String...)
     */
    public void setUnGenerateTable(String... tables) {
        getStrategyConfig().setUnGenerateTable(tables);
    }

    /**
     * @see TemplateConfig#getTemplate()
     */
    public ITemplate getTemplateEngine() {
        return getTemplateConfig().getTemplate();
    }

    /**
     * @see TemplateConfig#setTemplate(ITemplate)
     */
    public void setTemplateEngine(ITemplate template) {
        getTemplateConfig().setTemplate(template);
    }

    /**
     * @see TemplateConfig#getEntity()
     */
    public String getEntityTemplatePath() {
        return getTemplateConfig().getEntity();
    }


    /**
     * @see TemplateConfig#getMapper()
     */
    public String getMapperTemplatePath() {
        return getTemplateConfig().getMapper();
    }


    /**
     * @see TemplateConfig#getService()
     */
    public String getServiceTemplatePath() {
        return getTemplateConfig().getService();
    }


    /**
     * @see TemplateConfig#getServiceImpl()
     */
    public String getServiceImplTemplatePath() {
        return getTemplateConfig().getServiceImpl();
    }


    /**
     * @see TemplateConfig#getController()
     */
    public String getControllerTemplatePath() {
        return getTemplateConfig().getController();
    }


    /**
     * @see TemplateConfig#getTableDef()
     */
    public String getTableDefTemplatePath() {
        return getTemplateConfig().getTableDef();
    }


    /**
     * @see TemplateConfig#getMapperXml()
     */
    public String getMapperXmlTemplatePath() {
        return getTemplateConfig().getMapperXml();
    }

    public Boolean getFrontTableGenerateEnable() {
        return frontTableGenerateEnable;
    }

    public GlobalConfig setFrontTableGenerateEnable(Boolean frontTableGenerateEnable) {
        this.frontTableGenerateEnable = frontTableGenerateEnable;
        return this;
    }

    public Boolean getFrontTreeGenerateEnable() {
        return frontTreeGenerateEnable;
    }

    public GlobalConfig setFrontTreeGenerateEnable(Boolean frontTreeGenerateEnable) {
        this.frontTreeGenerateEnable = frontTreeGenerateEnable;
        return this;
    }

    public Boolean isEntityGenerateEnable() {
        return entityGenerateEnable;
    }

    /**
     * @see #enableEntity()
     * @see #disableEntity()
     */
    public void setEntityGenerateEnable(Boolean entityGenerateEnable) {
        this.entityGenerateEnable = entityGenerateEnable;
    }

    public Boolean isVoGenerateEnable() {
        return voGenerateEnable;
    }

    /**
     * @see #enableVo()
     * @see #disableVo()
     */
    public void setVoGenerateEnable(Boolean voGenerateEnable) {
        this.voGenerateEnable = voGenerateEnable;
    }

    public Boolean isDtoGenerateEnable() {
        return dtoGenerateEnable;
    }

    public Boolean isControllerGenerateEnable() {
        return controllerGenerateEnable;
    }


    /**
     * @see #enableDto()
     * @see #disableDto()
     */
    public void setDtoGenerateEnable(Boolean dtoGenerateEnable) {
        this.dtoGenerateEnable = dtoGenerateEnable;
    }

    public Boolean isQueryGenerateEnable() {
        return queryGenerateEnable;
    }

    /**
     * @see #enableQuery()
     * @see #disableQuery()
     */
    public void setQueryGenerateEnable(Boolean queryGenerateEnable) {
        this.queryGenerateEnable = queryGenerateEnable;
    }

    /**
     * @see EntityConfig#getClassPrefix()
     */
    public String getEntityClassPrefix() {
        return getEntityConfig().getClassPrefix();
    }

    /**
     * @see EntityConfig#setClassPrefix(String)
     */
    public void setEntityClassPrefix(String entityClassPrefix) {
        getEntityConfig().setClassPrefix(entityClassPrefix);
    }

    /**
     * @see EntityConfig#getClassSuffix()
     */
    public String getEntityClassSuffix() {
        return getEntityConfig().getClassSuffix();
    }

    /**
     * @see EntityConfig#setClassSuffix(String)
     */
    public void setEntityClassSuffix(String entityClassSuffix) {
        getEntityConfig().setClassSuffix(entityClassSuffix);
    }

    /**
     * @see EntityConfig#getSuperClass()
     */
    public Class<?> getEntitySuperClass() {
        return getEntityConfig().getSuperClass();
    }

    /**
     * @see EntityConfig#setSuperClass(Class)
     */
    public void setEntitySuperClass(Class<?> entitySuperClass) {
        getEntityConfig().setSuperClass(entitySuperClass);
    }

    /**
     * @see EntityConfig#getSuperClassFactory()
     */
    public Function<Table, Class<?>> getEntitySuperClassFactory() {
        return getEntityConfig().getSuperClassFactory();
    }

    /**
     * @see EntityConfig#setSuperClassFactory(Function)
     */
    public void setEntitySuperClassFactory(Function<Table, Class<?>> superClassFactory) {
        getEntityConfig().setSuperClassFactory(superClassFactory);
    }

    /**
     * @see EntityConfig#getImplInterfaces()
     */
    public Class<?>[] getEntityInterfaces() {
        return getEntityConfig().getImplInterfaces();
    }

    /**
     * @see EntityConfig#setImplInterfaces(Class[])
     */
    public void setEntityInterfaces(Class<?>[] entityInterfaces) {
        getEntityConfig().setImplInterfaces(entityInterfaces);
    }

    /**
     * @see EntityConfig#getWithLombok()
     */
    public Boolean isEntityWithLombok() {
        return getEntityConfig().getWithLombok();
    }

    /**
     * @see EntityConfig#setWithLombok(Boolean)
     */
    public void setEntityWithLombok(Boolean entityWithLombok) {
        getEntityConfig().setWithLombok(entityWithLombok);
    }

    /**
     * @see EntityConfig#getWithSwagger()
     */
    public Boolean isEntityWithSwagger() {
        return getEntityConfig().getWithSwagger();
    }

    /**
     * @see EntityConfig#getSwaggerVersion()
     */
    public EntityConfig.SwaggerVersion getSwaggerVersion() {
        return getEntityConfig().getSwaggerVersion();
    }

    /**
     * @see EntityConfig#setWithSwagger(Boolean)
     */
    public void setEntityWithSwagger(Boolean entityWithSwagger) {
        getEntityConfig().setWithSwagger(entityWithSwagger);
    }

    /**
     * @see EntityConfig#getWithActiveRecord()
     */
    public Boolean isWithActiveRecord() {
        return getEntityConfig().getWithActiveRecord();
    }

    /**
     * @see EntityConfig#setWithActiveRecord(Boolean)
     */
    public void setWithActiveRecord(Boolean withActiveRecord) {
        getEntityConfig().setWithActiveRecord(withActiveRecord);
    }

    /**
     * @see EntityConfig#getDataSource()
     */
    public String getEntityDataSource() {
        return getEntityConfig().getDataSource();
    }

    /**
     * @see EntityConfig#setDataSource(String)
     */
    public void setEntityDataSource(String dataSource) {
        getEntityConfig().setDataSource(dataSource);
    }

    /**
     * @see EntityConfig#getJdkVersion()
     */
    public int getEntityJdkVersion() {
        return getEntityConfig().getJdkVersion();
    }

    /**
     * @see EntityConfig#setJdkVersion(int)
     */
    public void setEntityJdkVersion(int jdkVersion) {
        getEntityConfig().setJdkVersion(jdkVersion);
    }

    /**
     * @see EntityConfig#setJdkVersion(int)
     */
    public void setJdkVersion(int jdkVersion) {
        getEntityConfig().setJdkVersion(jdkVersion);
        getVoConfig().setJdkVersion(jdkVersion);
        getQueryConfig().setJdkVersion(jdkVersion);
        getDtoConfig().setJdkVersion(jdkVersion);
    }

    public Boolean isMapperGenerateEnable() {
        return mapperGenerateEnable;
    }

    /**
     * @see #enableMapper()
     * @see #disableMapper()
     */
    public void setMapperGenerateEnable(Boolean mapperGenerateEnable) {
        this.mapperGenerateEnable = mapperGenerateEnable;
    }


    /**
     * @see MapperConfig#getClassPrefix()
     */
    public String getMapperClassPrefix() {
        return getMapperConfig().getClassPrefix();
    }

    /**
     * @see MapperConfig#setClassPrefix(String)
     */
    public void setMapperClassPrefix(String mapperClassPrefix) {
        getMapperConfig().setClassPrefix(mapperClassPrefix);
    }

    /**
     * @see MapperConfig#getClassSuffix()
     */
    public String getMapperClassSuffix() {
        return getMapperConfig().getClassSuffix();
    }

    /**
     * @see MapperConfig#setClassSuffix(String)
     */
    public void setMapperClassSuffix(String mapperClassSuffix) {
        getMapperConfig().setClassSuffix(mapperClassSuffix);
    }

    /**
     * @see MapperConfig#getSuperClass()
     */
    public Class<?> getMapperSuperClass() {
        return getMapperConfig().getSuperClass();
    }

    /**
     * @see MapperConfig#setSuperClass(Class)
     */
    public void setMapperSuperClass(Class<?> mapperSuperClass) {
        getMapperConfig().setSuperClass(mapperSuperClass);
    }

    /**
     * @see MapperConfig#getMapperAnnotation()
     */
    public Boolean getMapperAnnotation() {
        return getMapperConfig().getMapperAnnotation();
    }

    /**
     * @see MapperConfig#setMapperAnnotation(Boolean)
     */
    public void setMapperAnnotation(Boolean mapperAnnotation) {
        getMapperConfig().setMapperAnnotation(mapperAnnotation);
    }

    public Boolean getServiceGenerateEnable() {
        return serviceGenerateEnable;
    }

    /**
     * @see #enableService()
     * @see #disableService()
     */
    public void setServiceGenerateEnable(Boolean serviceGenerateEnable) {
        this.serviceGenerateEnable = serviceGenerateEnable;
    }

    /**
     * @see ServiceConfig#getClassPrefix()
     */
    public String getServiceClassPrefix() {
        return getServiceConfig().getClassPrefix();
    }

    /**
     * @see ServiceConfig#setClassPrefix(String)
     */
    public void setServiceClassPrefix(String serviceClassPrefix) {
        getServiceConfig().setClassPrefix(serviceClassPrefix);
    }

    /**
     * @see ServiceConfig#getClassSuffix()
     */
    public String getServiceClassSuffix() {
        return getServiceConfig().getClassSuffix();
    }

    /**
     * @see ServiceConfig#setClassSuffix(String)
     */
    public void setServiceClassSuffix(String serviceClassSuffix) {
        getServiceConfig().setClassSuffix(serviceClassSuffix);
    }

    /**
     * @see ServiceConfig#getSuperClass()
     */
    public Class<?> getServiceSuperClass() {
        return getServiceConfig().getSuperClass();
    }

    /**
     * @see ServiceConfig#setSuperClass(Class)
     */
    public void setServiceSuperClass(Class<?> serviceSuperClass) {
        getServiceConfig().setSuperClass(serviceSuperClass);
    }

    public Boolean isServiceImplGenerateEnable() {
        return serviceImplGenerateEnable;
    }

    /**
     * @see #enableServiceImpl()
     * @see #disableServiceImpl()
     */
    public void setServiceImplGenerateEnable(Boolean serviceImplGenerateEnable) {
        this.serviceImplGenerateEnable = serviceImplGenerateEnable;
    }


    /**
     * @see ServiceImplConfig#getClassPrefix()
     */
    public String getServiceImplClassPrefix() {
        return getServiceImplConfig().getClassPrefix();
    }

    /**
     * @see ServiceImplConfig#setClassPrefix(String)
     */
    public void setServiceImplClassPrefix(String serviceImplClassPrefix) {
        getServiceImplConfig().setClassPrefix(serviceImplClassPrefix);
    }

    /**
     * @see ServiceImplConfig#getClassSuffix()
     */
    public String getServiceImplClassSuffix() {
        return getServiceImplConfig().getClassSuffix();
    }

    /**
     * @see ServiceImplConfig#setClassSuffix(String)
     */
    public void setServiceImplClassSuffix(String serviceImplClassSuffix) {
        getServiceImplConfig().setClassSuffix(serviceImplClassSuffix);
    }

    /**
     * @see ServiceImplConfig#getSuperClass()
     */
    public Class<?> getServiceImplSuperClass() {
        return getServiceImplConfig().getSuperClass();
    }

    /**
     * @see ServiceImplConfig#setSuperClass(Class)
     */
    public void setServiceImplSuperClass(Class<?> serviceImplSuperClass) {
        getServiceImplConfig().setSuperClass(serviceImplSuperClass);
    }

    /**
     * @see #enableController()
     * @see #disableController()
     */
    public void setControllerGenerateEnable(Boolean controllerGenerateEnable) {
        this.controllerGenerateEnable = controllerGenerateEnable;
    }

    /**
     * @see ControllerConfig#getRequestMappingPrefix()
     */
    public String getControllerRequestMappingPrefix() {
        return getControllerConfig().getRequestMappingPrefix();
    }

    /**
     * @see ControllerConfig#setRequestMappingPrefix(String)
     */
    public void setControllerRequestMappingPrefix(String controllerRequestMappingPrefix) {
        getControllerConfig().setRequestMappingPrefix(controllerRequestMappingPrefix);
    }

    /**
     * @see ControllerConfig#getClassPrefix()
     */
    public String getControllerClassPrefix() {
        return getControllerConfig().getClassPrefix();
    }

    /**
     * @see ControllerConfig#setClassPrefix(String)
     */
    public void setControllerClassPrefix(String controllerClassPrefix) {
        getControllerConfig().setClassPrefix(controllerClassPrefix);
    }

    /**
     * @see ControllerConfig#getClassSuffix()
     */
    public String getControllerClassSuffix() {
        return getControllerConfig().getClassSuffix();
    }

    /**
     * @see ControllerConfig#setClassSuffix(String)
     */
    public void setControllerClassSuffix(String controllerClassSuffix) {
        getControllerConfig().setClassSuffix(controllerClassSuffix);
    }

    /**
     * @see ControllerConfig#getSuperClass()
     */
    public Class<?> getControllerSuperClass() {
        return getControllerConfig().getSuperClass();
    }

    /**
     * @see ControllerConfig#setSuperClass(Class)
     */
    public void setControllerSuperClass(Class<?> controllerSuperClass) {
        getControllerConfig().setSuperClass(controllerSuperClass);
    }

    /**
     * @see ControllerConfig#getRestStyle()
     */
    public Boolean isControllerRestStyle() {
        return getControllerConfig().getRestStyle();
    }

    /**
     * @see ControllerConfig#setRestStyle(Boolean)
     */
    public void setControllerRestStyle(Boolean restStyle) {
        getControllerConfig().setRestStyle(restStyle);
    }

    public Boolean isTableDefGenerateEnable() {
        return tableDefGenerateEnable;
    }

    /**
     * @see #enableTableDef()
     * @see #disableTableDef()
     */
    public void setTableDefGenerateEnable(Boolean tableDefGenerateEnable) {
        this.tableDefGenerateEnable = tableDefGenerateEnable;
    }

    /**
     * @see TableDefConfig#getClassPrefix()
     */
    public String getTableDefClassPrefix() {
        return getTableDefConfig().getClassPrefix();
    }

    /**
     * @see TableDefConfig#setClassPrefix(String)
     */
    public void setTableDefClassPrefix(String tableDefClassPrefix) {
        getTableDefConfig().setClassPrefix(tableDefClassPrefix);
    }

    /**
     * @see TableDefConfig#getClassSuffix()
     */
    public String getTableDefClassSuffix() {
        return getTableDefConfig().getClassSuffix();
    }

    /**
     * @see TableDefConfig#setClassSuffix(String)
     */
    public void setTableDefClassSuffix(String tableDefClassSuffix) {
        getTableDefConfig().setClassSuffix(tableDefClassSuffix);
    }

    /**
     * @see TableDefConfig#getPropertiesNameStyle()
     */
    public TableDefConfig.NameStyle getTableDefPropertiesNameStyle() {
        return getTableDefConfig().getPropertiesNameStyle();
    }

    /**
     * @see TableDefConfig#setPropertiesNameStyle(TableDefConfig.NameStyle)
     */
    public void setTableDefPropertiesNameStyle(TableDefConfig.NameStyle propertiesNameStyle) {
        getTableDefConfig().setPropertiesNameStyle(propertiesNameStyle);
    }

    /**
     * @see TableDefConfig#getInstanceSuffix()
     */
    public String getTableDefInstanceSuffix() {
        return getTableDefConfig().getInstanceSuffix();
    }

    /**
     * @see TableDefConfig#setInstanceSuffix(String)
     */
    public void setTableDefInstanceSuffix(String instanceSuffix) {
        getTableDefConfig().setInstanceSuffix(instanceSuffix);
    }

    public Boolean isMapperXmlGenerateEnable() {
        return mapperXmlGenerateEnable;
    }

    /**
     * @see #enableMapperXml()
     * @see #disableMapperXml()
     */
    public void setMapperXmlGenerateEnable(Boolean mapperXmlGenerateEnable) {
        this.mapperXmlGenerateEnable = mapperXmlGenerateEnable;
    }


    /**
     * @see MapperXmlConfig#getFilePrefix()
     */
    public String getMapperXmlFilePrefix() {
        return getMapperXmlConfig().getFilePrefix();
    }

    /**
     * @see MapperXmlConfig#setFilePrefix(String)
     */
    public void setMapperXmlFilePrefix(String mapperXmlFilePrefix) {
        getMapperXmlConfig().setFilePrefix(mapperXmlFilePrefix);
    }

    /**
     * @see MapperXmlConfig#getFileSuffix()
     */
    public String getMapperXmlFileSuffix() {
        return getMapperXmlConfig().getFileSuffix();
    }

    /**
     * @see MapperXmlConfig#setFileSuffix(String)
     */
    public void setMapperXmlFileSuffix(String mapperXmlFileSuffix) {
        getMapperXmlConfig().setFileSuffix(mapperXmlFileSuffix);
    }

    public Boolean isPackageInfoGenerateEnable() {
        return packageInfoGenerateEnable;
    }

    /**
     * @see #enablePackageInfo()
     * @see #disablePackageInfo()
     */
    public void setPackageInfoGenerateEnable(Boolean packageInfoGenerateEnable) {
        this.packageInfoGenerateEnable = packageInfoGenerateEnable;
    }

}
