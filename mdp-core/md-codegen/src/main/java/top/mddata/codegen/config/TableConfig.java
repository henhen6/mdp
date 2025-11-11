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
package top.mddata.codegen.config;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.SetListener;
import com.mybatisflex.annotation.UpdateListener;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 表的单独设置。
 */
@Data
@Accessors(chain = true)
public class TableConfig implements Serializable {

    public static final String ALL_TABLES = "*";
    @Serial
    private static final long serialVersionUID = -2568968178699265858L;

    /**
     * 数据库的 schema（模式）。
     */
    private String schema;

    /**
     * 表名。
     */
    private String tableName = ALL_TABLES;

    /**
     * 默认为 驼峰属性 转换为 下划线字段。
     */
    private Boolean camelToUnderline;

    /**
     * 监听 entity 的 insert 行为。
     */
    private Class<? extends InsertListener> insertListenerClass;

    /**
     * 监听 entity 的 update 行为。
     */
    private Class<? extends UpdateListener> updateListenerClass;

    /**
     * 监听 entity 的查询数据的 set 行为。
     */
    private Class<? extends SetListener> setListenerClass;

    /**
     * 是否开启 Mapper 生成。
     */
    private Boolean mapperGenerateEnable;

    /**
     * 对应列的配置。
     */
    private Map<String, ColumnConfig> columnConfigMap;


    public static TableConfig create() {
        return new TableConfig();
    }

    public static Builder builder() {
        return new Builder();
    }

    public TableConfig setColumnConfig(ColumnConfig columnConfig) {
        if (this.columnConfigMap == null) {
            this.columnConfigMap = new HashMap<>();
        }
        this.columnConfigMap.put(columnConfig.getColumnName(), columnConfig);
        return this;
    }

    protected ColumnConfig getColumnConfig(String columnName) {
        return this.columnConfigMap == null ? null : this.columnConfigMap.get(columnName);
    }

    public static final class Builder {

        private final TableConfig tableConfig;

        private Builder() {
            this.tableConfig = new TableConfig();
        }

        public Builder schema(String schema) {
            this.tableConfig.setSchema(schema);
            return this;
        }

        public Builder tableName(String tableName) {
            this.tableConfig.setTableName(tableName);
            return this;
        }

        public Builder camelToUnderline(Boolean camelToUnderline) {
            this.tableConfig.setCamelToUnderline(camelToUnderline);
            return this;
        }

        public Builder insertListenerClass(Class<? extends InsertListener> insertListenerClass) {
            this.tableConfig.setInsertListenerClass(insertListenerClass);
            return this;
        }

        public Builder updateListenerClass(Class<? extends UpdateListener> updateListenerClass) {
            this.tableConfig.setUpdateListenerClass(updateListenerClass);
            return this;
        }

        public Builder setListenerClass(Class<? extends SetListener> setListenerClass) {
            this.tableConfig.setSetListenerClass(setListenerClass);
            return this;
        }

        public Builder mapperGenerateEnable(Boolean mapperGenerateEnable) {
            this.tableConfig.setMapperGenerateEnable(mapperGenerateEnable);
            return this;
        }

        public Builder columnConfig(ColumnConfig columnConfigMap) {
            this.tableConfig.setColumnConfig(columnConfigMap);
            return this;
        }

        public TableConfig build() {
            return this.tableConfig;
        }

    }

}
