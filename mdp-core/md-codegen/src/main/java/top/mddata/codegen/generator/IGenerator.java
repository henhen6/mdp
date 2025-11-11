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
package top.mddata.codegen.generator;

import top.mddata.codegen.config.GlobalConfig;
import top.mddata.codegen.constant.GenTypeEnum;
import top.mddata.codegen.entity.Table;

/**
 * 文件生成器接口。
 *
 * @author Michael Yang
 * @author 王帅
 */
public interface IGenerator {
    /**
     *  代码路径
     *
     * @param table 表
     * @param globalConfig  全局配置
     * @param absolute true 绝对路径； false 相对路径
     * @return 代码路径
     */
    String getFilePath(Table table, GlobalConfig globalConfig, boolean absolute);

    /**
     *  生成器类型
     *
     * @return 生成器类型
     */
    GenTypeEnum getGenType();

    /**
     * 根据模板生成文件。
     *
     * @param table        表内容
     * @param templateContent  模版内容
     * @param globalConfig 全局配置
     */
    void generate(Table table, GlobalConfig globalConfig, String templateContent);

    /**
     * 预览代码
     *
     * @param table        表内容
     * @param globalConfig 全局配置
     * @return 代码
     */
    String preview(Table table, GlobalConfig globalConfig);

}
