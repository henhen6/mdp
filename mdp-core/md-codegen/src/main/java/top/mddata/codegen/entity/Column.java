/*
 *  Copyright (c) 2022-2023, Mybatis-Flex (fuhai999@gmail.com).
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

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.annotation.ColumnMask;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.mask.MaskManager;
import com.mybatisflex.core.mask.Masks;
import com.mybatisflex.core.util.StringUtil;
import top.mddata.codegen.config.ColumnConfig;
import top.mddata.codegen.config.EntityConfig;
import top.mddata.codegen.config.JavadocConfig;
import top.mddata.codegen.config.front.FormConfig;
import top.mddata.codegen.config.front.ListConfig;
import top.mddata.codegen.config.front.PropertyConfig;
import top.mddata.codegen.config.front.SearchConfig;

import java.math.BigDecimal;
import java.sql.ResultSetMetaData;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 数据库表里面的列信息。
 */
public class Column {

    /**
     * 字段名称。
     */
    private String name;

    /**
     * 属性名称。
     */
    private String property;

    /**
     * 属性类型。
     */
    private String propertyType;


    /**
     * 属性TS类型。
     */
    private String tsType;

    /**
     * 字段注释。
     */
    private String comment;

    /**
     * 是否可为空。
     */
    private int nullable;

    /**
     * 是否为主键。
     */
    private Boolean primaryKey = false;

    /**
     * 是否自增。
     */
    private Boolean autoIncrement;

    /**
     * 数据库的字段类型，比如 varchar/tinyint 等
     */
    private String rawType;

    /**
     * 数据库中的字段长度，比如 varchar(32) 中的 32
     */
    private int rawLength;
    /**
     * 数据库中的字段精度，比如 decimal(10,2) 中的 2
     */
    private int rawScale;

    /**
     * 字段配置。
     */
    private ColumnConfig columnConfig;

    private EntityConfig entityConfig;
    private JavadocConfig javadocConfig;
    /** 列表配置 */
    private ListConfig listConfig;
    private FormConfig formConfig;
    private SearchConfig searchConfig;
    private PropertyConfig propertyConfig;

    /**
     * importClass为类的全限定名
     */
    private static void addImportClass(Set<String> importClasses, String importClass) {
        importClass = importClass.trim();

        // java.util.List<String> >>>>> java.util.List
        if (importClass.contains("<") && importClass.endsWith(">")) {
            importClass = importClass.substring(0, importClass.indexOf("<"));
        }

        // 不包含“.”则认为是原始类型，不需要import
        // lang 包不需要显式导入
        if (importClass.contains(".") && !importClass.startsWith("java.lang.")) {
            importClasses.add(importClass);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.property = buildPropertyName();
    }

    public String getProperty() {
        return property;
    }

    public String getTsType() {
        return tsType;
    }

    public Column setTsType(String tsType) {
        this.tsType = tsType;
        return this;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public String getPropertyDefaultValue() {
        return columnConfig.getPropertyDefaultValue();
    }

    public String getPropertySimpleType() {
        if (columnConfig.getPropertyType() != null) {
            if (!columnConfig.getPropertyType().contains(".")) {
                return columnConfig.getPropertyType();
            }
            return StringUtil.substringAfterLast(columnConfig.getPropertyType(), ".");
        } else {
            return StringUtil.substringAfterLast(propertyType, ".");
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSwaggerComment() {
        return getJavadocConfig().formatColumnSwaggerComment(comment);
    }

    public int getNullable() {
        return nullable;
    }

    public void setNullable(int nullable) {
        this.nullable = nullable;
    }

    public Boolean getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Boolean getAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(Boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public String getRawType() {
        return rawType;
    }

    public void setRawType(String rawType) {
        this.rawType = rawType;
    }

    public int getRawLength() {
        return rawLength;
    }

    public void setRawLength(int rawLength) {
        this.rawLength = rawLength;
    }

    public int getRawScale() {
        return rawScale;
    }

    public void setRawScale(int rawScale) {
        this.rawScale = rawScale;
    }

    public ColumnConfig getColumnConfig() {
        return columnConfig;
    }

    public void setColumnConfig(ColumnConfig columnConfig) {
        this.columnConfig = columnConfig;
    }

    public EntityConfig getEntityConfig() {
        return entityConfig;
    }

    public void setEntityConfig(EntityConfig entityConfig) {
        this.entityConfig = entityConfig;
    }

    public JavadocConfig getJavadocConfig() {
        return javadocConfig;
    }

    public void setJavadocConfig(JavadocConfig javadocConfig) {
        this.javadocConfig = javadocConfig;
    }

    public String getterMethod() {
        return "get" + StringUtil.firstCharToUpperCase(property);
    }

    public String setterMethod() {
        return "set" + StringUtil.firstCharToUpperCase(property);
    }

    public String buildComment() {
        if (StringUtil.noText(comment)) {
            return "";
        } else {
            return "/**\n" +
                   "     * " + comment + "\n" +
                   "     */";
        }
    }

    public String buildPropertyName() {
        String entityJavaFileName = name;
        return StringUtil.firstCharToLowerCase(StringUtil.underlineToCamel(entityJavaFileName));
    }

    private void addComma(StringBuilder annotations, Boolean needComma) {
        if (needComma) {
            annotations.append(", ");
        }
    }

    public String buildAnnotations() {
        StringBuilder annotations = new StringBuilder();

        //@Id 的注解
        if (primaryKey || columnConfig.getPrimaryKey()) {
            annotations.append("@Id(");

            boolean needComma = false;
            if (autoIncrement) {
                annotations.append("keyType = KeyType.Auto");
                needComma = true;
            } else if (columnConfig.getKeyType() != null) {
                annotations.append("keyType = KeyType.").append(columnConfig.getKeyType().name());
                needComma = true;
            }

            if (columnConfig.getKeyValue() != null) {
                addComma(annotations, needComma);
                annotations.append("value = \"").append(columnConfig.getKeyValue()).append("\"");
                needComma = true;
            }

            if (columnConfig.getKeyBefore() != null) {
                addComma(annotations, needComma);
                annotations.append("before = ").append(columnConfig.getKeyBefore());
                needComma = true;
            }


            if (entityConfig != null && entityConfig.getColumnCommentEnable() && StringUtil.hasText(comment)) {
                addComma(annotations, needComma);
                annotations.append("comment = \"")
                        .append(this.comment.replace("\n", "")
                                .replace("\"", "\\\"")
                                .trim())
                        .append("\"");
            }

            if (annotations.length() == 4) {
                annotations.deleteCharAt(annotations.length() - 1);
            } else {
                annotations.append(")");
            }

            if (entityConfig != null && entityConfig.getAlwaysGenColumnAnnotation()) {
                annotations.append("\n    ");
            }
        }

        boolean needGenColumnAnnotation = (entityConfig != null && entityConfig.getAlwaysGenColumnAnnotation())
                                          || !name.equalsIgnoreCase(StringUtil.camelToUnderline(property))
                                          || (entityConfig != null && entityConfig.getColumnCommentEnable() && StringUtil.hasText(this.comment) && annotations.length() == 0);

        StringBuilder columnAnnotation = new StringBuilder("@Column(");

        //@Column 注解
        if (columnConfig.getOnInsertValue() != null
            || columnConfig.getOnUpdateValue() != null
            || columnConfig.getLarge() != null
            || columnConfig.getLogicDelete() != null
            || columnConfig.getVersion() != null
            || columnConfig.getJdbcType() != null
            || columnConfig.getTypeHandler() != null
            || columnConfig.getTenantId() != null
            || needGenColumnAnnotation
        ) {
            boolean needComma = false;
            if (entityConfig != null && entityConfig.getAlwaysGenColumnAnnotation()
                || !name.equalsIgnoreCase(StringUtil.camelToUnderline(property))) {
                columnAnnotation.append("value = \"").append(name).append("\"");
                needComma = true;
            }

            if (columnConfig.getOnInsertValue() != null) {
                addComma(columnAnnotation, needComma);
                columnAnnotation.append("onInsertValue = \"").append(columnConfig.getOnInsertValue()).append("\"");
                needComma = true;
            }
            if (columnConfig.getOnUpdateValue() != null) {
                addComma(columnAnnotation, needComma);
                columnAnnotation.append("onUpdateValue = \"").append(columnConfig.getOnUpdateValue()).append("\"");
                needComma = true;
            }
            if (columnConfig.getLarge() != null) {
                addComma(columnAnnotation, needComma);
                columnAnnotation.append("isLarge = ").append(columnConfig.getLarge());
                needComma = true;
            }
            if (columnConfig.getLogicDelete() != null) {
                addComma(columnAnnotation, needComma);
                columnAnnotation.append("isLogicDelete = ").append(columnConfig.getLogicDelete());
                needComma = true;
            }
            if (columnConfig.getVersion() != null) {
                addComma(columnAnnotation, needComma);
                columnAnnotation.append("version = ").append(columnConfig.getVersion());
                needComma = true;
            }
            if (columnConfig.getJdbcType() != null) {
                addComma(columnAnnotation, needComma);
                columnAnnotation.append("jdbcType = JdbcType.").append(columnConfig.getJdbcType().name());
                needComma = true;
            }
            if (columnConfig.getTypeHandler() != null) {
                addComma(columnAnnotation, needComma);
                columnAnnotation.append("typeHandler = ").append(columnConfig.getTypeHandler().getSimpleName()).append(".class");
                needComma = true;
            }
            if (Boolean.TRUE.equals(columnConfig.getTenantId())) {
                addComma(columnAnnotation, needComma);
                columnAnnotation.append("tenantId = true");
                needComma = true;
            }
            if (entityConfig != null && entityConfig.getColumnCommentEnable() && StringUtil.hasText(comment)) {
                addComma(columnAnnotation, needComma);
                columnAnnotation.append("comment = \"")
                        .append(this.comment.replace("\n", "")
                                .replace("\"", "\\\"")
                                .trim())
                        .append("\"");
            }
            columnAnnotation.append(")");

            // @Column(value = "user_name") -> @Column("user_name")
            int index = columnAnnotation.indexOf(",");
            if (index == -1) {
                int start = columnAnnotation.indexOf("value");
                if (start != -1) {
                    columnAnnotation.delete(start, start + 8);
                }
            }

            annotations.append(columnAnnotation);
        }

        // @ColumnMask 注解
        String maskType = columnConfig.getMaskType();
        if (maskType != null) {
            if (annotations.length() != 0) {
                annotations.append("\n    ");
            }
            annotations.append("@ColumnMask(");
            if (MaskManager.getProcessorMap().containsKey(maskType)) {
                // @ColumnMask(Masks.MOBILE)
                annotations.append("Masks.").append(maskType.toUpperCase());
            } else {
                // @ColumnMask("custom")
                annotations.append("\"").append(maskType).append("\"");
            }
            annotations.append(")");
        }

        return annotations.toString();
    }

    public String buildValidatorAnnotations() {
        StringBuilder annotations = new StringBuilder();

        if (primaryKey || columnConfig.getPrimaryKey() || nullable == ResultSetMetaData.columnNoNulls) {
            String notAnt = "@NotNull";
            if (String.class.getName().equals(propertyType)) {
                notAnt = "@NotEmpty";
            }
            annotations.append(notAnt);
            annotations.append(StrUtil.format("(message = \"请填写{}\"", getSwaggerComment()));
            if (primaryKey || columnConfig.getPrimaryKey()) {
                annotations.append(", groups = BaseEntity.Update.class");
            }
            annotations.append(")");
            if (StrUtil.equalsAny(propertyType, String.class.getName(), BigDecimal.class.getName())) {
                annotations.append("\n    ");
            }
        }

        if (String.class.getName().equals(propertyType)) {
            annotations.append(StrUtil.format("@Size(max = {}, message = \"{}长度不能超过{max}\")", rawLength, getSwaggerComment()));
        } else if (BigDecimal.class.getName().equals(propertyType)) {
            annotations.append(StrUtil.format("@Digits(integer = {}, fraction = {}, message = \"{}整数位长度不能超过{integer}, 小数位长度不能超过{fraction}\")", rawLength, rawScale, getSwaggerComment()));
        }

//        else if ("java.lang.Integer".equals(propertyType)) {
//            annotations.append(StrUtil.format("@Max(value = {}, message = \"{}不能大于{value}\")", "Integer.MAX_VALUE", getSwaggerComment()));
//            annotations.append("\n    ");
//            annotations.append(StrUtil.format("@Min(value = {}, message = \"{}不能小于{value}\")", "Integer.MIN_VALUE", getSwaggerComment()));
//        } else if ("java.lang.Long".equals(propertyType)) {
//            annotations.append(StrUtil.format("@Max(value = {}, message = \"{}不能大于{value}\")", "Integer.MAX_VALUE", getSwaggerComment()));
//            annotations.append("\n    ");
//            annotations.append(StrUtil.format("@Min(value = {}, message = \"{}不能小于{value}\")", "Integer.MIN_VALUE", getSwaggerComment()));
//        } else if ("java.lang.Short".equals(propertyType)) {
//            annotations.append(StrUtil.format("@Max(value = {}, message = \"{}不能大于{value}\")", "Short.MAX_VALUE", getSwaggerComment()));
//            annotations.append("\n    ");
//            annotations.append(StrUtil.format("@Min(value = {}, message = \"{}不能小于{value}\")", "Short.MIN_VALUE", getSwaggerComment()));
//        } else if ("java.lang.Byte".equals(propertyType)) {
//            annotations.append(StrUtil.format("@Max(value = {}, message = \"{}不能大于{value}\")", "Byte.MAX_VALUE", getSwaggerComment()));
//            annotations.append("\n    ");
//            annotations.append(StrUtil.format("@Min(value = {}, message = \"{}不能小于{value}\")", "Byte.MIN_VALUE", getSwaggerComment()));
//        }

        return annotations.toString();
    }

    public Set<String> getImportClasses() {
        Set<String> importClasses = new LinkedHashSet<>();

        addImportClass(importClasses, propertyType);

        if (primaryKey || (columnConfig != null && columnConfig.getPrimaryKey())) {
            addImportClass(importClasses, Id.class.getName());
            if (autoIncrement || (columnConfig != null && columnConfig.getKeyType() != null)) {
                addImportClass(importClasses, KeyType.class.getName());
            }
        }

        if (columnConfig != null) {
            if (columnConfig.getPropertyType() != null) {
                addImportClass(importClasses, columnConfig.getPropertyType());
            }
            if (columnConfig.getMaskType() != null) {
                addImportClass(importClasses, ColumnMask.class.getName());
                if (MaskManager.getProcessorMap().containsKey(columnConfig.getMaskType())) {
                    addImportClass(importClasses, Masks.class.getName());
                }
            }

            if (columnConfig.getJdbcType() != null) {
                addImportClass(importClasses, "org.apache.ibatis.type.JdbcType");
            }

            if (columnConfig.getTypeHandler() != null) {
                addImportClass(importClasses, columnConfig.getTypeHandler().getName());
            }

            Boolean needGenColumnAnnotation = (entityConfig != null && entityConfig.getAlwaysGenColumnAnnotation())
                                              || !name.equalsIgnoreCase(StringUtil.camelToUnderline(property))
                                              || (entityConfig != null && entityConfig.getColumnCommentEnable() && StringUtil.hasText(this.comment));

            if (columnConfig.getOnInsertValue() != null
                || columnConfig.getOnUpdateValue() != null
                || columnConfig.getLarge() != null
                || columnConfig.getLogicDelete() != null
                || columnConfig.getVersion() != null
                || columnConfig.getJdbcType() != null
                || columnConfig.getTypeHandler() != null
                || Boolean.TRUE.equals(columnConfig.getTenantId())
                || needGenColumnAnnotation
            ) {
                addImportClass(importClasses, com.mybatisflex.annotation.Column.class.getName());
            }
        }

        return importClasses;
    }

    public Boolean getDefaultColumn() {
        if (columnConfig == null) {
            return true;
        }
        boolean isLarge = columnConfig.getLarge() != null && columnConfig.getLarge();
        boolean isLogicDelete = columnConfig.getLogicDelete() != null && columnConfig.getLogicDelete();
        return !isLarge && !isLogicDelete;
    }

    public ListConfig getListConfig() {
        return listConfig;
    }

    public Column setListConfig(ListConfig listConfig) {
        this.listConfig = listConfig;
        return this;
    }

    public FormConfig getFormConfig() {
        return formConfig;
    }

    public Column setFormConfig(FormConfig formConfig) {
        this.formConfig = formConfig;
        return this;
    }

    public SearchConfig getSearchConfig() {
        return searchConfig;
    }

    public Column setSearchConfig(SearchConfig searchConfig) {
        this.searchConfig = searchConfig;
        return this;
    }

    public PropertyConfig getPropertyConfig() {
        return propertyConfig;
    }

    public Column setPropertyConfig(PropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
        return this;
    }

    @Override
    public String toString() {
        return "Column{" +
               "name='" + name + '\'' +
               ", className='" + propertyType + '\'' +
               ", remarks='" + comment + '\'' +
               ", isAutoIncrement=" + autoIncrement +
               '}';
    }

}
