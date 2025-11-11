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
package top.mddata.codegen.entity;

import top.mddata.codegen.config.ControllerConfig;
import top.mddata.codegen.config.DtoConfig;
import top.mddata.codegen.config.EntityConfig;
import top.mddata.codegen.config.GlobalConfig;
import top.mddata.codegen.config.MapperConfig;
import top.mddata.codegen.config.MapperXmlConfig;
import top.mddata.codegen.config.PackageConfig;
import top.mddata.codegen.config.QueryConfig;
import top.mddata.codegen.config.ServiceConfig;
import top.mddata.codegen.config.ServiceImplConfig;
import top.mddata.codegen.config.TableConfig;
import top.mddata.codegen.config.TableDefConfig;
import top.mddata.codegen.config.VoConfig;
import top.mddata.codegen.constant.PackageConst;
import top.mddata.base.annotation.log.RequestLog;
import top.mddata.base.base.entity.TreeEntity;
import top.mddata.base.model.cache.CacheKeyBuilder;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据库表信息。
 */
public class Table {

    /**
     * 表名。
     */
    private String name;

    /**
     * schema（模式）。
     */
    private String schema;

    /**
     * 表注释。
     */
    private String comment;

    /**
     * 主键。
     */
    private Set<String> primaryKeys;

    /**
     * 子类的的列。
     */
    private List<Column> columns = new ArrayList<>();
    /**
     * 所有列。
     */
    private List<Column> allColumns = new ArrayList<>();
    /**
     * 父类的列。
     */
    private List<Column> superColumns = new ArrayList<>();
    private List<String> superColumnNames = new ArrayList<>();

    /**
     * 表配置。
     */
    private TableConfig tableConfig;

    private EntityConfig entityConfig;

    /**
     * 全局配置。
     */
    private GlobalConfig globalConfig;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        if (StringUtil.hasText(comment)) {
            return globalConfig.getJavadocConfig().formatTableComment(comment);
        }
        return null;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSwaggerComment() {
        if (StringUtil.hasText(comment)) {
            return globalConfig.getJavadocConfig().formatTableSwaggerComment(comment);
        }
        return null;
    }

    public Column getPrimaryKey() {
        // 这里默认表中一定会有字段，就不做空判断了
        List<Column> varAllColumns = new ArrayList<>();
        varAllColumns.addAll(columns);
        varAllColumns.addAll(superColumns);
        return varAllColumns.stream()
                .filter(Column::getPrimaryKey)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("PrimaryKey can't be null"));
    }

    public Set<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(Set<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public void addPrimaryKey(String primaryKey) {
        if (primaryKeys == null) {
            primaryKeys = new LinkedHashSet<>();
        }
        primaryKeys.add(primaryKey);
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Column> getSuperColumns() {
        return superColumns;
    }

    public List<Column> getAllColumns() {
        return allColumns;
    }

    public List<Column> getSortedColumns() {
        List<Column> arrayList = new ArrayList<>(columns);
        // 生成字段排序
        arrayList.sort(Comparator.comparingInt((Column c) -> c.getProperty().length())
                .thenComparing(Column::getProperty));
        return arrayList;
    }

    public List<Column> getSortedListColumns() {
        List<Column> arrayList = new ArrayList<>(columns);
        arrayList.sort(Comparator.comparingInt((Column c) -> c.getListConfig().getSequence())
                .thenComparing(Column::getProperty));
        return arrayList;
    }

    public List<Column> getSortedFormColumns() {
        List<Column> arrayList = new ArrayList<>(columns);
        arrayList.sort(Comparator.comparingInt((Column c) -> c.getFormConfig().getSequence())
                .thenComparing(Column::getProperty));
        return arrayList;
    }

    public List<Column> getSortedSearchColumns() {
        List<Column> arrayList = new ArrayList<>(columns);
        arrayList.sort(Comparator.comparingInt((Column c) -> c.getSearchConfig().getSequence())
                .thenComparing(Column::getProperty));
        return arrayList;
    }

    public boolean containsColumn(String columnName) {
        if (columns == null || columns.isEmpty() || StringUtil.noText(columnName)) {
            return false;
        }
        for (Column column : columns) {
            if (columnName.equals(column.getName())) {
                return true;
            }
        }
        return false;
    }

    public boolean containsColumn(String... columnNames) {
        for (String columnName : columnNames) {
            if (!containsColumn(columnName)) {
                return false;
            }
        }
        return true;
    }

    public boolean containsAnyColumn(String... columnNames) {
        for (String columnName : columnNames) {
            if (containsColumn(columnName)) {
                return true;
            }
        }
        return false;
    }

    public void addColumn(Column column) {
        if (CollUtil.isEmpty(superColumnNames)) {
            Class<?> superClass = entityConfig.getSuperClass(this);
            //获取所有 private字段
            if (superClass != null) {
                Field[] fields = ReflectUtil.getFields(superClass);
                for (Field field : fields) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers)) {
                        superColumnNames.add(field.getName());
                    }
                }
            }
        }

        //主键
        if (primaryKeys != null && primaryKeys.contains(column.getName())) {
            column.setPrimaryKey(true);
            if (column.getAutoIncrement() == null && (column.getPropertyType().equals(Integer.class.getName()) || column.getPropertyType().equals(BigInteger.class.getName()))) {
                column.setAutoIncrement(true);
            }
        }
        // 自增
        if (column.getAutoIncrement() == null) {
            column.setAutoIncrement(false);
        }

        column.setColumnConfig(globalConfig.getStrategyConfig().getColumnConfig(name, column.getName()));
        column.setEntityConfig(globalConfig.getEntityConfig());
        column.setJavadocConfig(globalConfig.getJavadocConfig());

        if (superColumnNames.contains(column.getProperty())) {
            superColumns.add(column);
            allColumns.add(column);
            return;
        }

        columns.add(column);
        allColumns.add(column);
    }

    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    public TableConfig getTableConfig() {
        return tableConfig;
    }

    public void setTableConfig(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }

    public EntityConfig getEntityConfig() {
        return entityConfig;
    }

    public void setEntityConfig(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
    }

    // ===== 构建实体类文件 =====

    /**
     * 构建 import 导包。
     */
    public List<String> buildImports(Boolean isBase) {
        Set<String> imports = new HashSet<>();

        //base 类不需要添加 Table 的导入，没有 @Table 注解
        if (!isBase) {
            imports.add("com.mybatisflex.annotation.Table");
        }

        EntityConfig varEntityConfig = globalConfig.getEntityConfig();

        //未开启基类生成，或者是基类的情况下，添加 Column 类型的导入
        if (!varEntityConfig.getWithBaseClassEnable() || (varEntityConfig.getWithBaseClassEnable() && isBase)) {
            for (Column column : columns) {
                imports.addAll(column.getImportClasses());
            }

            Class<?> superClass = varEntityConfig.getSuperClass(this);
            if (superClass != null) {
                imports.add(superClass.getName());
            }

            if (varEntityConfig.getImplInterfaces() != null) {
                for (Class<?> entityInterface : varEntityConfig.getImplInterfaces()) {
                    imports.add(entityInterface.getName());
                }
            }
        }


        if (!varEntityConfig.getWithBaseClassEnable() || (varEntityConfig.getWithBaseClassEnable() && !isBase)) {
            if (tableConfig != null) {
                if (tableConfig.getInsertListenerClass() != null) {
                    imports.add(tableConfig.getInsertListenerClass().getName());
                }
                if (tableConfig.getUpdateListenerClass() != null) {
                    imports.add(tableConfig.getUpdateListenerClass().getName());
                }
                if (tableConfig.getSetListenerClass() != null) {
                    imports.add(tableConfig.getSetListenerClass().getName());
                }
            }
        }

        return imports.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }

    public List<String> buildServiceImplImports() {
        Set<String> imports = new HashSet<>();

        ServiceImplConfig serviceImplConfig = globalConfig.getServiceImplConfig();
        if (BooleanUtil.isTrue(serviceImplConfig.getCache())) {
            imports.add(CacheKeyBuilder.class.getName());
        }
        imports.add(Slf4j.class.getName());
        imports.add(RequiredArgsConstructor.class.getName());

        return imports.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }

    public List<String> buildControllerImports() {
        Set<String> imports = new HashSet<>();
        String entityClassName = buildEntityClassName();
        String voClassName = buildVoClassName();
        String dtoClassName = buildDtoClassName();
        String queryClassName = buildQueryClassName();
        String serviceClassName = buildServiceClassName();
        PackageConfig packageConfig = globalConfig.getPackageConfig();
        ControllerConfig controllerConfig = globalConfig.getControllerConfig();
        EntityConfig.SwaggerVersion swaggerVersion = globalConfig.getSwaggerVersion();

        if (controllerConfig.getRestStyle()) {
            imports.add(PackageConst.REST_CONTROLLER);
        } else {
            imports.add(PackageConst.CONTROLLER);
        }

        if (EntityConfig.SwaggerVersion.FOX.getName().equals(swaggerVersion.getName())) {
            imports.add(PackageConst.API);
            if (controllerConfig.getWithCrud()) {
                imports.add(PackageConst.API_OPERATION);
                imports.add(PackageConst.API_PARAM);
            }
        } else {
            imports.add(PackageConst.TAG);
            if (controllerConfig.getWithCrud()) {
                imports.add(PackageConst.OPERATION);
                imports.add(PackageConst.PARAMETER);
            }
        }

        imports.add(PackageConst.VALIDATED);
        if (controllerConfig.getWithCrud()) {
            imports.add(RequestLog.class.getName());
            imports.add(RequiredArgsConstructor.class.getName());
            imports.add(PackageConst.R);
            imports.add(PackageConst.PAGE);
            imports.add(PackageConst.BASE_ENTITY);
            imports.add(PackageConst.REQUEST_BODY);
            imports.add(PackageConst.REQUEST_PARAM);
            imports.add(PackageConst.GET_MAPPING);
            imports.add(PackageConst.POST_MAPPING);
            imports.add(PackageConst.PAGE_PARAMS);
            imports.add(PackageConst.QUERY_WRAPPER);
            imports.add(PackageConst.CONTROLLER_UTIL);
            imports.add(List.class.getName());
            imports.add(BeanUtil.class.getName());
            imports.add(StrUtil.format("{}.{}", packageConfig.getServicePackage(), serviceClassName));


            imports.add(StrUtil.format("{}.{}", packageConfig.getEntityPackage(), entityClassName));
            imports.add(StrUtil.format("{}.{}", packageConfig.getVoPackage(), voClassName));
            imports.add(StrUtil.format("{}.{}", packageConfig.getQueryPackage(), queryClassName));
            imports.add(StrUtil.format("{}.{}", packageConfig.getDtoPackage(), dtoClassName));
        }
        imports.add(PackageConst.REQUEST_MAPPING);

        Class<?> superClass = controllerConfig.getSuperClass();
        if (superClass == null) {
            return imports.stream().filter(Objects::nonNull).sorted(Comparator.naturalOrder()).toList();
        }

        TypeVariable<? extends Class<?>>[] typeParameters = superClass.getTypeParameters();

        for (TypeVariable<? extends Class<?>> typeParameter : typeParameters) {

            switch (typeParameter.getTypeName()) {
                case "Id" -> imports.add(null);
                case "Entity" ->
                        imports.add(StrUtil.format("{}.{}", packageConfig.getEntityPackage(), entityClassName));
                case "VO" -> imports.add(StrUtil.format("{}.{}", packageConfig.getVoPackage(), voClassName));
                case "Query" -> imports.add(StrUtil.format("{}.{}", packageConfig.getQueryPackage(), queryClassName));
                case "DTO" -> imports.add(StrUtil.format("{}.{}", packageConfig.getDtoPackage(), dtoClassName));
                default -> imports.add(StrUtil.format("{}.{}", packageConfig.getServicePackage(), serviceClassName));
            }
        }

        imports.add(superClass.getName());

        return imports.stream().filter(Objects::nonNull).sorted(Comparator.naturalOrder()).toList();
    }

    /**
     * 构建 树结构 index.tsx 的导入
     * @return 导入
     */
    public List<String> buildTreeIndexTsxImports() {
        PackageConfig packageConfig = globalConfig.getPackageConfig();

        Set<String> imports = new HashSet<>();
        imports.add("import type { FormActionType } from './form';");
        imports.add("import type { TreeActionType } from './tree';");
        imports.add("import { ref, unref } from 'vue';");
        imports.add(StrUtil.format("import type { {}Type } from '#/api/{}/{}/{}/model';", buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(buildEntityClassName())));
        imports.add("import { ActionEnum } from '@vben/constants';");

        return imports.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }

    /**
     * 构建 树结构 tree.tsx 的导入
     * @return 导入
     */
    public List<String> buildTreeTreeTsxImports() {
        PackageConfig packageConfig = globalConfig.getPackageConfig();

        Set<String> imports = new HashSet<>();
        imports.add("import type { VxeTreePropTypes } from 'vxe-pc-ui';");
        imports.add(StrUtil.format("import type { {}Type } from '#/api/{}/{}/{}/model';", buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(buildEntityClassName())));

        imports.add("import { onMounted, reactive, ref, unref } from 'vue';");
        imports.add("import { findNodeByKey } from '@vben/plugins/vxe-tree';");
        imports.add(StrUtil.format("import { {}Api } from '#/api/{}/{}/{}/api';", buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(buildEntityClassName())));
        imports.add(StrUtil.format("import { {} as permCode } from '#/enums/perm/{}/{}/{}';", StringUtil.firstCharToLowerCase(buildEntityClassName()), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(buildEntityClassName())));
        imports.add("import { useMessage } from '@vben/components/hooks';");
        imports.add("import { $t } from '#/locales';");

        return imports.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }

    /**
     * 构建 树结构 move.tsx 的导入
     * @return 导入
     */
    public List<String> buildTreeMoveTsxImports() {
        PackageConfig packageConfig = globalConfig.getPackageConfig();

        Set<String> imports = new HashSet<>();
        imports.add(StrUtil.format("import type { {}Type } from '#/api/{}/{}/{}/model';", buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(buildEntityClassName())));

        imports.add("import { reactive, ref, unref } from 'vue';");
        imports.add("import { useVbenModal } from '@vben/common-ui';");

        imports.add(StrUtil.format("import { {}Api } from '#/api/{}/{}/{}/api';", buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(buildEntityClassName())));
        imports.add("import { useMessage } from '@vben/components/hooks';");


        return imports.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }

    /**
     * 构建 树结构 form.tsx 的导入
     * @return 导入
     */
    public List<String> buildTreeFormTsxImports() {
        PackageConfig packageConfig = globalConfig.getPackageConfig();

        Set<String> imports = new HashSet<>();
        imports.add("import type { VbenFormSchema } from '@vben/components/adapter';");
        imports.add("import type { FormSchemaExt } from '#/api';");
        imports.add(StrUtil.format("import type { {}Type } from '#/api/{}/{}/{}/model';", buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(buildEntityClassName())));
        imports.add("import type { ApiTreeSelect } from '#/components/form';");


        imports.add("import { computed, reactive } from 'vue';");
        imports.add("import { ActionEnum } from '@vben/constants';");
        imports.add("import { breakpointsTailwind, useBreakpoints } from '@vueuse/core';");
        imports.add("import { useVbenForm } from '@vben/components/adapter';");
        imports.add("import { getValidateRuleByVben } from '#/api/common/validate';");
        imports.add("import { useMessage } from '@vben/components/hooks';");
        imports.add("import { $t } from '#/locales';");


        imports.add(StrUtil.format("import { {}Api, {}Config } from '#/api/{}/{}/{}/api';", buildEntityClassName(), buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(buildEntityClassName())));


        return imports.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
    }


    /**
     * 构建 单表结构 index.tsx 的导入
     * @return 导入
     */
    public List<String> buildIndexTsxImports() {
        Set<String> imports = new HashSet<>();
        PackageConfig packageConfig = globalConfig.getPackageConfig();

        imports.add(StrUtil.format("import type { {}Type } from '#/api/{}/{}/{}/model';", buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(buildEntityClassName())));
        imports.add(StrUtil.format("import { {}Api } from '#/api/{}/{}/{}/api';", buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(buildEntityClassName())));
        imports.add(StrUtil.format("import { {} as permCode } from '#/enums/perm/{}/{}/{}';", StringUtil.firstCharToLowerCase(buildEntityClassName()), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(buildEntityClassName())));

        return imports.stream().filter(Objects::nonNull).sorted(Comparator.naturalOrder()).toList();
    }

    /**
     * 构建 单表结构 form.tsx 的导入
     * @return 导入
     */
    public List<String> buildFormTsxImports() {
        Set<String> imports = new HashSet<>();
        PackageConfig packageConfig = globalConfig.getPackageConfig();

        imports.add(StrUtil.format("import type { {}Type } from '#/api/{}/{}/{}/model';", buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(buildEntityClassName())));
        imports.add(StrUtil.format("import { {}Api, {}Config } from '#/api/{}/{}/{}/api';", buildEntityClassName(), buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(buildEntityClassName())));

        return imports.stream().filter(Objects::nonNull).sorted(Comparator.naturalOrder()).toList();
    }

    /**
     * 构建 @Table(...) 注解。
     */
    public String buildTableAnnotation() {
        StringBuilder tableAnnotation = new StringBuilder();

        String baseEntityClassName = buildEntityClassName();
        if (entityConfig.getWithBaseClassEnable()) {
            baseEntityClassName = buildEntityClassName() + entityConfig.getWithBaseClassSuffix();
        }
        String tableName = StrUtil.format("{}.TABLE_NAME", baseEntityClassName);
        tableAnnotation.append("@Table(value = ").append(tableName);

        String globalSchema;

        if (tableConfig == null) {
            // 未配置 tableConfig 以策略中的 schema 为主
            globalSchema = schema;
        } else if (StringUtil.noText(tableConfig.getSchema())) {
            // 配置 tableConfig 但未指定 schema 还是以策略中的 schema 为主
            globalSchema = schema;
        } else {
            // 以用户设置的 tableConfig 中的 schema 为主
            globalSchema = null;
        }

        if (StringUtil.hasText(globalSchema)) {
            tableAnnotation.append(", schema = \"").append(globalSchema).append("\"");
        }

        // 添加 dataSource 配置，因为代码生成器是一个数据源生成的，所以这些实体类应该都是一个数据源。
        String dataSource = globalConfig.getEntityDataSource();
        if (StringUtil.hasText(dataSource)) {
            tableAnnotation.append(", dataSource = \"").append(dataSource).append("\"");
        }


        if (tableConfig != null) {
            if (StringUtil.hasText(tableConfig.getSchema())) {
                tableAnnotation.append(", schema = \"").append(tableConfig.getSchema()).append("\"");
            }
            if (tableConfig.getCamelToUnderline() != null) {
                tableAnnotation.append(", camelToUnderline = ").append(tableConfig.getCamelToUnderline());
            }
            if (tableConfig.getInsertListenerClass() != null) {
                tableAnnotation.append(", onInsert = ").append(tableConfig.getInsertListenerClass().getSimpleName()).append(".class");
            }
            if (tableConfig.getUpdateListenerClass() != null) {
                tableAnnotation.append(", onUpdate = ").append(tableConfig.getUpdateListenerClass().getSimpleName()).append(".class");
            }
            if (tableConfig.getSetListenerClass() != null) {
                tableAnnotation.append(", onSet = ").append(tableConfig.getSetListenerClass().getSimpleName()).append(".class");
            }
            if (Boolean.FALSE.equals(tableConfig.getMapperGenerateEnable())) {
                tableAnnotation.append(", mapperGenerateEnable = false");
            }
        }


        if (entityConfig != null && entityConfig.getColumnCommentEnable() && StringUtil.hasText(comment)) {
            tableAnnotation.append(", comment = \"")
                    .append(this.comment.replace("\n", "").replace("\"", "\\\"").trim())
                    .append("\"");
        }

        // @Table(value = "sys_user") -> @Table("sys_user")
        int index = tableAnnotation.indexOf(",");
        if (index == -1) {
            int start = tableAnnotation.indexOf("value");
            if (start != -1) {
                tableAnnotation.delete(start, start + 8);
            }
        }

        return tableAnnotation.append(")").toString();
    }

    /**
     * 构建 extends 继承。
     */
    public String buildExtends(Boolean isBase) {
        EntityConfig varEntityConfig = globalConfig.getEntityConfig();
        Class<?> superClass = varEntityConfig.getSuperClass(this);
        if (superClass != null) {
            String type = "";
            if (varEntityConfig.isSuperClassGenericity(this)) {
                if (varEntityConfig.getGenericityType() == null) {
                    type = StrUtil.format("<{}{}>", buildEntityClassName(), isBase ? varEntityConfig.getWithBaseClassSuffix() : "");
                } else {
                    // 特殊处理
                    if (TreeEntity.class.getSimpleName().equals(superClass.getSimpleName())) {
                        type = StrUtil.format("<{}, E>", varEntityConfig.getGenericityType().getSimpleName());
                    } else {
                        type = StrUtil.format("<{}>", varEntityConfig.getGenericityType().getSimpleName());
                    }
                }
            }
            return " extends " + superClass.getSimpleName() + type;
        } else {
            return "";
        }
    }

    public String buildTreeExtends(Boolean isBase, Table table) {
        EntityConfig varEntityConfig = globalConfig.getEntityConfig();
        Class<?> superClass = varEntityConfig.getSuperClass(this);
        if (superClass != null) {
            if (TreeEntity.class.getSimpleName().equals(superClass.getSimpleName())) {
                if (isBase) {
                    return StrUtil.format("<E extends {}<{}, E>>", TreeEntity.class.getSimpleName(), varEntityConfig.getGenericityType().getSimpleName());
                } else {
                    return StrUtil.format("{}<{}>", buildBeanExtends(), buildEntityClassName());
                }
            } else {
                if (isBase) {
                    return "";
                } else {
                    return table.buildEntityClassName() + entityConfig.getWithBaseClassSuffix();
                }
            }
        }
        return "";
    }

    public String buildBeanExtends() {
        return buildEntityClassName() + entityConfig.getWithBaseClassSuffix();
    }


    /**
     * 构建 implements 实现。
     */
    public String buildImplements() {
        Class<?>[] entityInterfaces = globalConfig.getEntityConfig().getImplInterfaces();
        if (entityInterfaces != null && entityInterfaces.length > 0) {
            return " implements " + StringUtil.join(", ", Arrays.stream(entityInterfaces)
                    .map(Class::getSimpleName).collect(Collectors.toList()));
        } else {
            return "";
        }
    }

    /**
     * 构建 kt 继承
     */
    public String buildKtExtends(Boolean isBase) {
        EntityConfig varEntityConfig = globalConfig.getEntityConfig();
        Class<?> superClass = varEntityConfig.getSuperClass(this);
        List<String> s = new ArrayList<>();
        if (superClass != null) {
            String varName = superClass.getSimpleName();
            if (varEntityConfig.isSuperClassGenericity(this)) {
                varName += "<" + buildEntityClassName() + (isBase ? varEntityConfig.getWithBaseClassSuffix() : "") + ">";
            }
            varName += "()";
            s.add(varName);
        }
        Class<?>[] entityInterfaces = globalConfig.getEntityConfig().getImplInterfaces();
        if (entityInterfaces != null && entityInterfaces.length > 0) {
            for (Class<?> inter : entityInterfaces) {
                s.add(inter.getSimpleName());
            }
        }
        if (s.isEmpty()) {
            return "";
        }
        return " :" + String.join(",", s);
    }

    // ===== 构建相关类名 =====

    /**
     * 获取生成 Java 文件名。
     */
    public String getEntityJavaFileName() {
        String entityName = entityConfig.getName();
        if (StrUtil.isNotEmpty(entityName)) {
            return entityName;
        }
        String entityJavaFileName = name;
        String tablePrefix = globalConfig.getStrategyConfig().getTablePrefix();
        if (tablePrefix != null) {
            String[] tablePrefixes = tablePrefix.split(",");
            for (String prefix : tablePrefixes) {
                String trimPrefix = prefix.trim();
                if (trimPrefix.length() > 0 && name.startsWith(trimPrefix)) {
                    entityJavaFileName = name.substring(trimPrefix.length());
                    break;
                }
            }
        }
        return StringUtil.firstCharToUpperCase(StringUtil.underlineToCamel(entityJavaFileName));
    }

    /**
     * 构建 entity 的 Class 名称。
     */
    public String buildEntityClassName() {
        String entityJavaFileName = getEntityJavaFileName();
        EntityConfig varEntityConfig = globalConfig.getEntityConfig();
        return varEntityConfig.getClassPrefix()
               + entityJavaFileName
               + varEntityConfig.getClassSuffix();
    }

    /**
     * 构建 entity 的 变量 名称。
     */
    public String buildEntityVarName() {
        return StringUtil.firstCharToLowerCase(buildEntityClassName());
    }

    /**
     * 构建 entity 的 Class 名称。
     */
    public String buildEntityBaseClassName() {
        return buildEntityClassName() + entityConfig.getWithBaseClassSuffix();
    }

    /**
     * 构建 Vo 的 Class 名称。
     */
    public String buildVoClassName() {
        String entityJavaFileName = getEntityJavaFileName();
        VoConfig voConfig = globalConfig.getVoConfig();
        return voConfig.getClassPrefix()
               + entityJavaFileName
               + voConfig.getClassSuffix();
    }

    /**
     * 构建 Dto 的 Class 名称。
     */
    public String buildDtoClassName() {
        String entityJavaFileName = getEntityJavaFileName();
        DtoConfig voConfig = globalConfig.getDtoConfig();
        return voConfig.getClassPrefix()
               + entityJavaFileName
               + voConfig.getClassSuffix();
    }

    /**
     * 构建 Dto 的 Class 名称。
     */
    public String buildQueryClassName() {
        String entityJavaFileName = getEntityJavaFileName();
        QueryConfig voConfig = globalConfig.getQueryConfig();
        return voConfig.getClassPrefix()
               + entityJavaFileName
               + voConfig.getClassSuffix();
    }


    /**
     * 构建 tableDef 的 Class 名称。
     */
    public String buildTableDefClassName() {
        String tableDefJavaFileName = getEntityJavaFileName();
        TableDefConfig tableDefConfig = globalConfig.getTableDefConfig();
        return tableDefConfig.getClassPrefix()
               + tableDefJavaFileName
               + tableDefConfig.getClassSuffix();
    }

    /**
     * 构建 mapper 的 Class 名称。
     */
    public String buildMapperClassName() {
        String entityJavaFileName = getEntityJavaFileName();
        MapperConfig mapperConfig = globalConfig.getMapperConfig();
        return mapperConfig.getClassPrefix()
               + entityJavaFileName
               + mapperConfig.getClassSuffix();
    }

    /**
     * 构建 service 的 Class 名称。
     */
    public String buildServiceClassName() {
        String entityJavaFileName = getEntityJavaFileName();
        ServiceConfig serviceConfig = globalConfig.getServiceConfig();
        return serviceConfig.getClassPrefix()
               + entityJavaFileName
               + serviceConfig.getClassSuffix();
    }

    /**
     * 构建 serviceImpl 的 Class 名称。
     */
    public String buildServiceImplClassName() {
        String entityJavaFileName = getEntityJavaFileName();
        ServiceImplConfig serviceImplConfig = globalConfig.getServiceImplConfig();
        return serviceImplConfig.getClassPrefix()
               + entityJavaFileName
               + serviceImplConfig.getClassSuffix();
    }

    /**
     * 构建 controller 的 Class 名称。
     */
    public String buildControllerClassName() {
        String entityJavaFileName = getEntityJavaFileName();
        ControllerConfig controllerConfig = globalConfig.getControllerConfig();
        return controllerConfig.getClassPrefix()
               + entityJavaFileName
               + controllerConfig.getClassSuffix();
    }

    /**
     * 构建访问路径的前缀
     */
    public String buildControllerRequestMappingPrefix() {
        String mappingPrefix = globalConfig.getControllerConfig().getRequestMappingPrefix();
        return mappingPrefix == null ? "" : mappingPrefix.trim();
    }

    /**
     * 构建 MapperXml 的文件名称。
     */
    public String buildMapperXmlFileName() {
        String tableDefJavaFileName = getEntityJavaFileName();
        MapperXmlConfig mapperXmlConfig = globalConfig.getMapperXmlConfig();
        return mapperXmlConfig.getFilePrefix()
               + tableDefJavaFileName
               + mapperXmlConfig.getFileSuffix();
    }

    @Override
    public String toString() {
        return "Table{" +
               "schema'" + schema + '\'' +
               "name='" + name + '\'' +
               ", remarks='" + comment + '\'' +
               ", primaryKeys='" + primaryKeys + '\'' +
               ", columns=" + columns +
               '}';
    }

}
