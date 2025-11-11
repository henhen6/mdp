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

import com.mybatisflex.core.util.StringUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 表策略配置。
 *
 * @author 王帅
 * @since 2023-05-14
 */
@Data
@Accessors(chain = true)
public class StrategyConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 504853587703061034L;
    /**
     * 数据库表前缀，多个前缀用英文逗号（,） 隔开。
     */
    private String tablePrefix;

    /**
     * 逻辑删除的默认字段名称。
     */
    private String logicDeleteColumn;

    /**
     * 乐观锁的字段名称。
     */
    private String versionColumn;

    /**
     * 是否生成视图映射。
     */
    private Boolean generateForView = true;

    /**
     * 单独为某张表添加独立的配置。
     */
    private Map<String, TableConfig> tableConfigMap;

    /**
     * 设置某个列的全局配置。
     */
    private Map<String, ColumnConfig> columnConfigMap;

    /**
     * 需要生成的表在哪个模式下
     */
    private String generateSchema;
    /**
     * 生成哪些表，白名单。
     */
    private Set<String> generateTables;

    /**
     * 不生成哪些表，黑名单。
     */
    private Set<String> unGenerateTables;

    /**
     * 需要忽略的列 全局配置。
     */
    private Set<String> ignoreColumns;


    /**
     * 设置需要忽略的列  全局配置。
     */
    public StrategyConfig setIgnoreColumns(String... columns) {
        if (ignoreColumns == null) {
            ignoreColumns = new HashSet<>();
        }
        for (String column : columns) {
            if (column != null && !column.trim().isEmpty()) {
                ignoreColumns.add(column.trim().toLowerCase());
            }
        }
        return this;
    }

    /**
     * 获取表配置。
     */
    public TableConfig getTableConfig(String tableName) {
        if (tableConfigMap == null) {
            return null;
        }
        TableConfig tableConfig = tableConfigMap.get(tableName);
        return tableConfig != null ? tableConfig : tableConfigMap.get(TableConfig.ALL_TABLES);
    }

    /**
     * 设置表配置。
     */
    public StrategyConfig setTableConfig(TableConfig tableConfig) {
        if (tableConfigMap == null) {
            tableConfigMap = new HashMap<>();
        }
        tableConfigMap.put(tableConfig.getTableName(), tableConfig);
        return this;
    }

    /**
     * 获取列配置。
     */
    public ColumnConfig getColumnConfig(String tableName, String columnName) {
        ColumnConfig columnConfig = null;

        TableConfig tableConfig = getTableConfig(tableName);
        if (tableConfig != null) {
            columnConfig = tableConfig.getColumnConfig(columnName);
        }

        if (columnConfig == null && columnConfigMap != null) {
            columnConfig = columnConfigMap.get(columnName);
        }

        if (columnConfig == null) {
            columnConfig = new ColumnConfig();
        }

        //全局配置的逻辑删除
        if (columnName.equals(logicDeleteColumn) && columnConfig.getLogicDelete() == null) {
            columnConfig.setLogicDelete(true);
        }

        //全部配置的乐观锁版本
        if (columnName.equals(versionColumn) && columnConfig.getVersion() == null) {
            columnConfig.setVersion(true);
        }


        return columnConfig;
    }

    /**
     * 设置列配置。
     */
    public StrategyConfig setColumnConfig(ColumnConfig columnConfig) {
        if (columnConfigMap == null) {
            columnConfigMap = new HashMap<>();
        }
        columnConfigMap.put(columnConfig.getColumnName(), columnConfig);
        return this;
    }

    /**
     * 设置列配置。
     */
    public StrategyConfig setColumnConfig(String tableName, ColumnConfig columnConfig) {
        TableConfig tableConfig = getTableConfig(tableName);
        if (tableConfig == null) {
            tableConfig = new TableConfig();
            tableConfig.setTableName(tableName);
            setTableConfig(tableConfig);
        }

        tableConfig.setColumnConfig(columnConfig);

        return this;
    }

    /**
     * 设置生成哪些表。
     */
    public StrategyConfig setGenerateTable(String... tables) {
        if (generateTables == null) {
            generateTables = new HashSet<>();
        }

        for (String table : tables) {
            if (table != null && !table.trim().isEmpty()) {
                generateTables.add(table.trim());
            }
        }

        return this;
    }

    /**
     * 设置不生成哪些表。
     */
    public StrategyConfig setUnGenerateTable(String... tables) {
        if (unGenerateTables == null) {
            unGenerateTables = new HashSet<>();
        }

        for (String table : tables) {
            if (table != null && !table.trim().isEmpty()) {
                unGenerateTables.add(table.trim());
            }
        }

        return this;
    }


    public boolean isSupportGenerate(String table) {
        if (table == null || table.isEmpty()) {
            return true;
        }
        if (unGenerateTables != null) {
            for (String unGenerateTable : unGenerateTables) {
                // 使用正则表达式匹配表名
                String regex = unGenerateTable.replace("*", ".*");
                if (table.matches(regex)) {
                    return false;
                }
            }
        }
        //不配置指定比表名的情况下，支持所有表
        if (generateTables == null || generateTables.isEmpty()) {
            return true;
        }
        for (String generateTable : generateTables) {
            String regex = generateTable.replace("*", ".*");
            if (table.matches(regex)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 设置表前缀。
     */
    public StrategyConfig setTablePrefix(String... tablePrefix) {
        this.tablePrefix = StringUtil.join(",", tablePrefix);
        return this;
    }

}
