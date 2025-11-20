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

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import top.mddata.codegen.constant.GenerationStrategyEnum;
import top.mddata.codegen.entity.Table;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.TypeVariable;

/**
 * 生成 Controller 的配置。
 *
 * @author 王帅
 * @since 2023-05-15
 */
@Data
@Accessors(chain = true)
public class ControllerConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 8391630904705910611L;
    /**
     * 代码生成目录，当未配置时，使用 PackageConfig 的配置
     */
    private String sourceDir;
    /**
     *  RequestMapping注解，访问路径的前缀。
     */
    private String requestMappingPrefix;
    /**
     * Controller 类的前缀。
     */
    private String classPrefix = "";

    /**
     * Controller 类的后缀。
     */
    private String classSuffix = "Controller";

    /**
     * 自定义 Controller 的父类。
     */
    private Class<?> superClass;

    /**
     * 在Controller类中生成CRUD方法。
     */
    private Boolean withCrud = false;
    /**
     * 生成策略。
     */
    private GenerationStrategyEnum generationStrategy = GenerationStrategyEnum.OVERWRITE;

    /**
     * 生成 REST 风格的 Controller。
     */
    private Boolean restStyle = true;

    public String buildSuperClassImport() {
        return superClass.getName();
    }

    public String buildSuperClassName(Table table) {
        if (superClass == null) {
            return "";
        }
        String entityClassName = table.buildEntityClassName();
        String voClassName = table.buildVoClassName();
        String dtoClassName = table.buildDtoClassName();
        String queryClassName = table.buildQueryClassName();
        String serviceClassName = table.buildServiceClassName();

        Class<?> genericityType = table.getEntityConfig().getGenericityType();
        TypeVariable<? extends Class<?>>[] typeParameters = superClass.getTypeParameters();

        if (typeParameters.length > 1) {
            StringBuilder genericityStr = new StringBuilder("<");
            for (TypeVariable<? extends Class<?>> typeParameter : typeParameters) {

                switch (typeParameter.getTypeName()) {
                    case "Id" ->
                            genericityStr.append(genericityType != null ? genericityType.getSimpleName() : "Long").append(", ");
                    case "Entity" -> genericityStr.append(entityClassName).append(", ");
                    case "VO" -> genericityStr.append(voClassName).append(", ");
                    case "DTO" -> genericityStr.append(dtoClassName).append(", ");
                    case "Query" -> genericityStr.append(queryClassName).append(", ");
                    default -> genericityStr.append(serviceClassName).append(", ");
                }

            }
            String genericity = StrUtil.removeSuffix(genericityStr, ", ");

            return superClass.getSimpleName() + genericity + ">";
        }

        return superClass.getSimpleName();
    }
}
