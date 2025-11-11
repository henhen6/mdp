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
package top.mddata.codegen.dialect;

import top.mddata.codegen.entity.Column;
import top.mddata.codegen.entity.Table;
import com.mybatisflex.core.util.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字段类型映射。
 *
 * @author michael
 */
public class TsTypeMapping {

    private static final Map<String, String> MAPPING = new HashMap<>();
    private static JdbcTypeMapper typeMapper;

    static {
        registerMapping(Arrays.asList("tinyint", "bit"), "boolean");
        registerMapping(Arrays.asList("int", "float", "double"), "number");
//        registerMapping(Arrays.asList("longtext", "char", "text", "json", "enum", "clob", "blob", "binary", "bigint", "decimal"), "string");
    }

    private TsTypeMapping() {
    }

    public static void registerMapping(Class<?> from, Class<?> to) {
        registerMapping(from.getName(), to.getName());
    }

    public static void registerMapping(String from, String to) {
        MAPPING.put(from, to);
    }

    public static void registerMapping(List<String> froms, String to) {
        froms.forEach(from -> MAPPING.put(from, to));
    }

    public static Map<String, String> getMapping() {
        return MAPPING;
    }

    public static JdbcTypeMapper getTypeMapper() {
        return typeMapper;
    }

    public static void setTypeMapper(JdbcTypeMapper typeMapper) {
        TsTypeMapping.typeMapper = typeMapper;
    }

    public static String getType(String rawType, String jdbcType, Table table, Column column) {
        if (typeMapper != null) {
            String type = typeMapper.getType(rawType, jdbcType, table, column);
            if (StringUtil.hasText(type)) {
                return type;
            }
        }
        return MAPPING.getOrDefault(rawType.toLowerCase(), "string");
    }

    public interface JdbcTypeMapper {
        /**
         * 获取类型映射。
         *
         * @param rawType  原始类型
         * @param jdbcJavaType jdbc 类型
         * @param table    表
         * @param column   字段
         * @return 类型映射
         */
        String getType(String rawType, String jdbcJavaType, Table table, Column column);
    }

}
