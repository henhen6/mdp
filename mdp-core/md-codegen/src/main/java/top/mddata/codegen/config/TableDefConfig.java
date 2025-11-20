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
import top.mddata.codegen.constant.GenerationStrategyEnum;

import java.io.Serial;
import java.io.Serializable;

/**
 * 生成 TableDef 的配置。
 *
 * @author 王帅
 * @since 2023-05-15
 */
@Data
@Accessors(chain = true)
public class TableDefConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 8137903163796008036L;
    /**
     * 代码生成目录，当未配置时，使用 PackageConfig 的配置
     */
    private String sourceDir;
    /**
     * TableDef 类的前缀。
     */
    private String classPrefix = "";

    /**
     * TableDef 类的后缀。
     */
    private String classSuffix = "TableDef";
    /**
     * 生成策略。
     */
    private GenerationStrategyEnum generationStrategy = GenerationStrategyEnum.OVERWRITE;

    /**
     * 生成辅助类的字段风格。
     */
    private NameStyle propertiesNameStyle = NameStyle.UPPER_CASE;

    /**
     * 生成辅助类的引用常量名后缀。
     */
    private String instanceSuffix = "";

    public String buildFieldName(String property) {
        return switch (propertiesNameStyle) {
            case UPPER_CASE -> StringUtil.camelToUnderline(property).toUpperCase();
            case LOWER_CASE -> StringUtil.camelToUnderline(property).toLowerCase();
            case UPPER_CAMEL_CASE -> StringUtil.firstCharToUpperCase(property);
            default -> StringUtil.firstCharToLowerCase(property);
        };
    }


    public enum NameStyle {
        /** 命名规则 */
        UPPER_CASE,
        LOWER_CASE,
        UPPER_CAMEL_CASE,
        LOWER_CAMEL_CASE

    }

}
