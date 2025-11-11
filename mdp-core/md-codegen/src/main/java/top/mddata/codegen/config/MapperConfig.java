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

import top.mddata.codegen.constant.GenerationStrategyEnum;
import com.mybatisflex.core.BaseMapper;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 生成 Mapper 的配置。
 *
 * @author 王帅
 * @since 2023-05-15
 */
@Data
@Accessors(chain = true)
public class MapperConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1937442008907641534L;
    /**
     * 代码生成目录，当未配置时，使用 PackageConfig 的配置
     */
    private String sourceDir;

    /**
     * Mapper 类的前缀。
     */
    private String classPrefix = "";

    /**
     * Mapper 类的后缀。
     */
    private String classSuffix = "Mapper";

    /**
     * 自定义 Mapper 的父类。
     */
    private Class<?> superClass = BaseMapper.class;
    /**
     * 生成策略。
     */
    private GenerationStrategyEnum generationStrategy = GenerationStrategyEnum.OVERWRITE;

    /**
     * 是否添加 {@code @Mapper} 注解。
     */
    private Boolean mapperAnnotation = false;

    public String buildSuperClassImport() {
        return superClass.getName();
    }

    public String buildSuperClassName() {
        return superClass.getSimpleName();
    }

}
