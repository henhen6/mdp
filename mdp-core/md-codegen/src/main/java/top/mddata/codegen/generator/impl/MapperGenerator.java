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
import top.mddata.codegen.config.GlobalConfig;
import top.mddata.codegen.config.MapperConfig;
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

/**
 * Mapper 生成器。
 *
 * @author Michael Yang
 * @author 王帅
 */
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class MapperGenerator implements IGenerator {

    private GenTypeEnum genType;

    public MapperGenerator() {
        this(GenTypeEnum.MAPPER);
    }

    public MapperGenerator(GenTypeEnum genType) {
        this.genType = genType;
    }

    private static Map<String, Object> getParams(Table table, GlobalConfig globalConfig, String mapperClassName, MapperConfig config, PackageConfig packageConfig) {
        Map<String, Object> params = new HashMap<>(6);
        params.put("table", table);
        params.put("mapperClassName", mapperClassName);
        params.put("mapperConfig", config);
        params.put("packageConfig", packageConfig);
        params.put("javadocConfig", globalConfig.getJavadocConfig());
        params.putAll(globalConfig.getCustomConfig());
        return params;
    }

    @Override
    public String getFilePath(Table table, GlobalConfig globalConfig, boolean absolute) {
        PackageConfig packageConfig = globalConfig.getPackageConfig();
        MapperConfig config = globalConfig.getMapperConfig();
        String layerPackage = packageConfig.getMapperPackage();
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
        path += table.buildMapperClassName() + StrPool.DOT_JAVA;
        return path;
    }

    @Override
    public void generate(Table table, GlobalConfig globalConfig, String templateContent) {

        if (!globalConfig.isMapperGenerateEnable()) {
            return;
        }

        PackageConfig packageConfig = globalConfig.getPackageConfig();
        MapperConfig config = globalConfig.getMapperConfig();

        if (config.getGenerationStrategy() == GenerationStrategyEnum.IGNORE) {
            return;
        }

        String path = getFilePath(table, globalConfig, true);
        File javaFile = new File(path);

        if (config.getGenerationStrategy() == GenerationStrategyEnum.EXIST_IGNORE) {
            if (javaFile.exists()) {
                return;
            }
        }

        String mapperClassName = table.buildMapperClassName();
        if (javaFile.exists()) {
            if (config.getGenerationStrategy() == GenerationStrategyEnum.BACKUPS) {
                String now = DateUtils.format(LocalDateTime.now(), CHINESE_DATE_TIME_PATTERN);
                String newPath = StrUtil.replaceLast(path, StrPool.DOT_JAVA, "_Backups" + now + StrPool.DOT_JAVA);
                File newFile = new File(newPath);
                FileUtil.copy(javaFile, newFile, true);
            } else if (config.getGenerationStrategy() == GenerationStrategyEnum.ADD) {
                String now = DateUtils.format(LocalDateTime.now(), CHINESE_DATE_TIME_PATTERN);
                String newPath = StrUtil.replaceLast(path, StrPool.DOT_JAVA, "_Add" + now + StrPool.DOT_JAVA);
                javaFile = new File(newPath);
                mapperClassName += "_Add" + now;
            }
        }

        Map<String, Object> params = getParams(table, globalConfig, mapperClassName, config, packageConfig);

        if (StrUtil.isNotEmpty(templateContent)) {
            globalConfig.getTemplateConfig().getTemplate().generateByContent(params, templateContent, javaFile);
        } else {
            globalConfig.getTemplateConfig().getTemplate().generate(params, genType.getTemplate(), javaFile);
        }

        log.info("Mapper ---> {}", javaFile);
    }

    @Override
    public String preview(Table table, GlobalConfig globalConfig) {
        PackageConfig packageConfig = globalConfig.getPackageConfig();
        MapperConfig config = globalConfig.getMapperConfig();

        Map<String, Object> params = getParams(table, globalConfig, table.buildMapperClassName(), config, packageConfig);

        return globalConfig.getTemplateConfig().getTemplate().previewByFile(params, genType.getTemplate());
    }
}
