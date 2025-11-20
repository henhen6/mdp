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
import top.mddata.codegen.config.EntityConfig;
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

/**
 * Entity Base 生成器。
 *
 * @author henhen6
 * @since 2024年06月19日23:47:34
 */
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class EntityBaseGenerator implements IGenerator {

    /** 模板路径 */
    private GenTypeEnum genType;

    public EntityBaseGenerator() {
        this(GenTypeEnum.ENTITY_BASE);
    }

    public EntityBaseGenerator(GenTypeEnum genType) {
        this.genType = genType;
    }

    private static String getEntityPackageName(EntityConfig config, String layerPackage) {
        String packageName;
        if (StringUtil.hasText(config.getWithBasePackage())) {
            packageName = config.getWithBasePackage().replace(".", "/");
        } else {
            packageName = layerPackage.replace(".", "/") + "/base";
        }
        return packageName;
    }

    @Override
    public String getFilePath(Table table, GlobalConfig globalConfig, boolean absolute) {
        PackageConfig packageConfig = globalConfig.getPackageConfig();
        EntityConfig config = globalConfig.getEntityConfig();
        String sourceDir = config.getSourceDir();

        String path = "";
        if (absolute) {
            path = StringUtil.hasText(sourceDir) ? sourceDir : packageConfig.getSourceDir();
            if (!path.endsWith(StrPool.SLASH)) {
                path += StrPool.SLASH;
            }
        }

        path += StrPool.SRC_MAIN_JAVA + StrPool.SLASH;

        String packageName = getEntityPackageName(config, packageConfig.getEntityPackage());
        path += packageName + StrPool.SLASH;

        String baseEntityClassName = table.buildEntityClassName() + config.getWithBaseClassSuffix();
        path += baseEntityClassName + StrPool.DOT_JAVA;
        return path;
    }

    @Override
    public void generate(Table table, GlobalConfig globalConfig, String templateContent) {
        if (!globalConfig.isEntityGenerateEnable()) {
            return;
        }
        EntityConfig entityConfig = globalConfig.getEntityConfig();
        if (entityConfig.getGenerationStrategy() == GenerationStrategyEnum.IGNORE) {
            return;
        }

        // 不需要生成 baseClass
        if (!entityConfig.getWithBaseClassEnable()) {
            return;
        }

        String path = getFilePath(table, globalConfig, true);
        File javaFile = new File(path);

        if (entityConfig.getGenerationStrategy() == GenerationStrategyEnum.EXIST_IGNORE) {
            if (javaFile.exists()) {
                return;
            }
        }

        String entityClassName = table.buildEntityClassName();
        if (javaFile.exists()) {
            if (entityConfig.getGenerationStrategy() == GenerationStrategyEnum.BACKUPS) {
                String now = DateUtils.format(LocalDateTime.now(), CHINESE_DATE_TIME_PATTERN);
                String newPath = StrUtil.replaceLast(path, StrPool.DOT_JAVA, "_Backups" + now + StrPool.DOT_JAVA);
                File newFile = new File(newPath);
                FileUtil.copy(javaFile, newFile, true);
            } else if (entityConfig.getGenerationStrategy() == GenerationStrategyEnum.ADD) {
                String now = DateUtils.format(LocalDateTime.now(), CHINESE_DATE_TIME_PATTERN);
                String newPath = StrUtil.replaceLast(path, StrPool.DOT_JAVA, "_Add" + now + StrPool.DOT_JAVA);
                javaFile = new File(newPath);
                entityClassName += "_Add" + now;
            }
        }


        String baseEntityClassName = table.buildEntityClassName() + entityConfig.getWithBaseClassSuffix();
        Map<String, Object> params = getTemplatePath(table, globalConfig, baseEntityClassName);

        log.info("BaseEntity ---> {}", javaFile);
        if (StrUtil.isNotEmpty(templateContent)) {
            globalConfig.getTemplateConfig().getTemplate().generateByContent(params, templateContent, javaFile);
        } else {
            globalConfig.getTemplateConfig().getTemplate().generate(params, genType.getTemplate(), javaFile);
        }
    }

    @Override
    public String preview(Table table, GlobalConfig globalConfig) {
        EntityConfig entityConfig = globalConfig.getEntityConfig();

        String baseEntityClassName = table.buildEntityClassName() + entityConfig.getWithBaseClassSuffix();

        Map<String, Object> params = getTemplatePath(table, globalConfig, baseEntityClassName);

        return globalConfig.getTemplateConfig().getTemplate().previewByFile(params, genType.getTemplate());
    }


    public Map<String, Object> getTemplatePath(Table table, GlobalConfig globalConfig, String baseEntityClassName) {
        Map<String, Object> params = new HashMap<>(8);
        EntityConfig entityConfig = globalConfig.getEntityConfig();
        PackageConfig packageConfig = globalConfig.getPackageConfig();

        // 排除忽略列
        if (globalConfig.getStrategyConfig().getIgnoreColumns() != null) {
            table.getColumns().removeIf(column -> globalConfig.getStrategyConfig().getIgnoreColumns().contains(column.getName().toLowerCase()));
        }

        String baseEntityPackagePath = getEntityPackageName(entityConfig, packageConfig.getEntityPackage());

        params.put("table", table);
        params.put("entityPackageName", baseEntityPackagePath.replace("/", "."));
        params.put("entityClassName", baseEntityClassName);
        params.put("entityConfig", entityConfig);
        params.put("packageConfig", packageConfig);
        params.put("javadocConfig", globalConfig.getJavadocConfig());
        params.put("isBase", true);
        params.putAll(globalConfig.getCustomConfig());
        return params;
    }
}
