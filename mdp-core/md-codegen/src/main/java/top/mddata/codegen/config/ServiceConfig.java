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
import com.mybatisflex.core.service.IService;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 生成 Service 的配置。
 *
 * @author 王帅
 * @since 2023-05-15
 */
@Data
@Accessors(chain = true)
public class ServiceConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = -2152473328300910220L;
    /**
     * 代码生成目录，当未配置时，使用 PackageConfig 的配置
     */
    private String sourceDir;

    /**
     * Service 类的前缀。
     */
    private String classPrefix = "";

    /**
     * Service 类的后缀。
     */
    private String classSuffix = "Service";

    /**
     * 自定义 Service 的父类。
     */
    private Class<?> superClass = IService.class;

    /**
     * 生成策略。
     */
    private GenerationStrategyEnum generationStrategy = GenerationStrategyEnum.OVERWRITE;

    public String buildSuperClassImport() {
        return superClass.getName();
    }

    public String buildSuperClassName() {
        return superClass.getSimpleName();
    }

}
