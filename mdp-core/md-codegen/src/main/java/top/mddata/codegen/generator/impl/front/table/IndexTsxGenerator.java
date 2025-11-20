/*
 * Copyright (c) 2024. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package top.mddata.codegen.generator.impl.front.table;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import top.mddata.base.utils.DateUtils;
import top.mddata.base.utils.StrPool;
import top.mddata.codegen.config.FrontConfig;
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
 * index.tsx 生成器。
 *
 * @author henhen6
 * @since 2024年06月18日15:48:18
 */
@Slf4j
@Getter
@Setter
@Accessors(chain = true)
public class IndexTsxGenerator implements IGenerator {

    private GenTypeEnum genType;

    public IndexTsxGenerator() {
        this(GenTypeEnum.INDEX_TSX);
    }

    public IndexTsxGenerator(GenTypeEnum genType) {
        this.genType = genType;
    }

    private static Map<String, Object> buildParam(FrontConfig config, Table table, GlobalConfig globalConfig, PackageConfig packageConfig) {
        Map<String, Object> params = new HashMap<>(7);
        params.put("config", config);
        params.put("table", table);
        params.put("voClassName", table.buildVoClassName());
        params.put("queryClassName", table.buildQueryClassName());
        params.put("javadocConfig", globalConfig.getJavadocConfig());
        params.put("packageConfig", packageConfig);
        params.put("globalConfig", globalConfig);
        return params;
    }

    @Override
    public String getFilePath(Table table, GlobalConfig globalConfig, boolean absolute) {
        PackageConfig packageConfig = globalConfig.getPackageConfig();

        String path = "";
        if (absolute) {
            path = packageConfig.getFrontSourceDir();
            if (!path.endsWith(StrPool.SLASH)) {
                path += StrPool.SLASH;
            }
        }

        path += "src" + StrPool.SLASH;
        path += "views" + StrPool.SLASH;
        // TODO 这里怎么取值
        path += packageConfig.getSubSystem() + StrPool.SLASH;
        path += packageConfig.getModule() + StrPool.SLASH;
        path += StringUtil.firstCharToLowerCase(table.buildEntityClassName()) + StrPool.SLASH;
        path += "data" + StrPool.SLASH;
        path += genType.getType();
        return path;
    }

    @Override
    public void generate(Table table, GlobalConfig globalConfig, String templateContent) {
        if (!globalConfig.getFrontTableGenerateEnable()) {
            return;
        }

        PackageConfig packageConfig = globalConfig.getPackageConfig();
        FrontConfig config = globalConfig.getFrontConfig();

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
            }
        }

        Map<String, Object> params = buildParam(config, table, globalConfig, packageConfig);

        log.info("index.tsx ---> {}", javaFile);
        if (StrUtil.isNotEmpty(templateContent)) {
            globalConfig.getTemplateConfig().getTemplate().generateByContent(params, templateContent, javaFile);
        } else {
            globalConfig.getTemplateConfig().getTemplate().generate(params, genType.getTemplate(), javaFile);
        }
    }

    @Override
    public String preview(Table table, GlobalConfig globalConfig) {
        PackageConfig packageConfig = globalConfig.getPackageConfig();
        FrontConfig config = globalConfig.getFrontConfig();

        Map<String, Object> params = buildParam(config, table, globalConfig, packageConfig);

        return globalConfig.getTemplateConfig().getTemplate().previewByFile(params, this.genType.getTemplate());
    }
}
