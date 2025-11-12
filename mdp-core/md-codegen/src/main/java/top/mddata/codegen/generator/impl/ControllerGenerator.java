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
package top.mddata.codegen.generator.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import top.mddata.base.utils.DateUtils;
import top.mddata.base.utils.StrPool;
import top.mddata.codegen.config.ControllerConfig;
import top.mddata.codegen.config.GlobalConfig;
import top.mddata.codegen.config.PackageConfig;
import top.mddata.codegen.constant.GenTypeEnum;
import top.mddata.codegen.constant.GenerationStrategyEnum;
import top.mddata.codegen.entity.Table;
import top.mddata.codegen.generator.IGenerator;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static cn.hutool.core.date.DatePattern.CHINESE_DATE_TIME_PATTERN;
import static com.mybatisflex.core.util.StringUtil.firstCharToLowerCase;

/**
 * Controller 生成器。
 *
 * @author 王帅
 * @since 2023-05-14
 */
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class ControllerGenerator implements IGenerator {

    private GenTypeEnum genType;


    public ControllerGenerator() {
        this(GenTypeEnum.CONTROLLER);
    }

    public ControllerGenerator(GenTypeEnum genType) {
        this.genType = genType;
    }

    private static Map<String, Object> getParams(Table table, GlobalConfig globalConfig, String controllerClassName, PackageConfig packageConfig, ControllerConfig controllerConfig) {
        Map<String, Object> params = new HashMap<>();
        params.put("controllerClassName", controllerClassName);
        params.put("table", table);
        params.put("packageConfig", packageConfig);
        params.put("controllerConfig", controllerConfig);
        params.put("javadocConfig", globalConfig.getJavadocConfig());
        params.put("withSwagger", globalConfig.isEntityWithSwagger());
        params.put("swaggerVersion", globalConfig.getSwaggerVersion());

        if (controllerConfig.getSuperClass() == null && controllerConfig.getWithCrud()) {
            params.put("serviceVarName", firstCharToLowerCase(table.buildServiceClassName()));
        } else {
            params.put("serviceVarName", "superService");
        }
        params.putAll(globalConfig.getCustomConfig());
        return params;
    }

    @Override
    public String getFilePath(Table table, GlobalConfig globalConfig, boolean absolute) {

        PackageConfig packageConfig = globalConfig.getPackageConfig();
        String layerPackage = packageConfig.getControllerPackage();
        ControllerConfig config = globalConfig.getControllerConfig();
        String sourceDir = config.getSourceDir();

        String path = "";
        if (absolute) {
            path = StringUtil.hasText(sourceDir) ? sourceDir : packageConfig.getSourceDir();
            if (!path.endsWith(StrPool.SLASH)) {
                path += StrPool.SLASH;
            }
        }

        path += StrPool.SRC_MAIN_JAVA + StrPool.SLASH;
        path += layerPackage.replace(StrPool.DOT, StrPool.SLASH) + StrPool.SLASH;
        path += table.buildControllerClassName() + StrPool.DOT_JAVA;
        return path;
    }

    @Override
    public void generate(Table table, GlobalConfig globalConfig, String templateContent) {
        if (!globalConfig.isControllerGenerateEnable()) {
            return;
        }


        PackageConfig packageConfig = globalConfig.getPackageConfig();
        ControllerConfig controllerConfig = globalConfig.getControllerConfig();
        if (controllerConfig.getGenerationStrategy() == GenerationStrategyEnum.IGNORE) {
            return;
        }

        String path = getFilePath(table, globalConfig, true);
        File controllerJavaFile = new File(path);

        if (controllerConfig.getGenerationStrategy() == GenerationStrategyEnum.EXIST_IGNORE) {
            if (controllerJavaFile.exists()) {
                return;
            }
        }

        String controllerClassName = table.buildControllerClassName();
        if (controllerJavaFile.exists()) {
            if (controllerConfig.getGenerationStrategy() == GenerationStrategyEnum.BACKUPS) {
                String now = DateUtils.format(LocalDateTime.now(), CHINESE_DATE_TIME_PATTERN);
                String newPath = StrUtil.replaceLast(path, StrPool.DOT_JAVA, "_Backups" + now + StrPool.DOT_JAVA);
                File newFile = new File(newPath);
                FileUtil.copy(controllerJavaFile, newFile, true);
            } else if (controllerConfig.getGenerationStrategy() == GenerationStrategyEnum.ADD) {
                String now = DateUtils.format(LocalDateTime.now(), CHINESE_DATE_TIME_PATTERN);
                String newPath = StrUtil.replaceLast(path, StrPool.DOT_JAVA, "_Add" + now + StrPool.DOT_JAVA);
                controllerJavaFile = new File(newPath);
                controllerClassName += "_Add" + now;
            }
        }

        Map<String, Object> params = getParams(table, globalConfig, controllerClassName, packageConfig, controllerConfig);

        if (StrUtil.isNotEmpty(templateContent)) {
            globalConfig.getTemplateConfig().getTemplate().generateByContent(params, templateContent, controllerJavaFile);
        } else {
            globalConfig.getTemplateConfig().getTemplate().generate(params, genType.getTemplate(), controllerJavaFile);
        }

        log.info("Controller ---> {}", controllerJavaFile);
    }

    @Override
    public String preview(Table table, GlobalConfig globalConfig) {
        PackageConfig packageConfig = globalConfig.getPackageConfig();
        ControllerConfig controllerConfig = globalConfig.getControllerConfig();

        Map<String, Object> params = getParams(table, globalConfig, table.buildControllerClassName(), packageConfig, controllerConfig);

        return globalConfig.getTemplateConfig().getTemplate().previewByFile(params, genType.getTemplate());
    }


}
