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
package top.mddata.codegen.template.impl;

import top.mddata.codegen.template.ITemplate;
import com.jfinal.template.Engine;
import com.jfinal.template.expr.ast.FieldGetters;
import com.jfinal.template.source.ClassPathSource;
import com.jfinal.template.source.FileSource;
import com.jfinal.template.source.ISource;
import com.jfinal.template.source.ISourceFactory;
import com.mybatisflex.core.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

/**
 * <a href=https://jfinal.com/doc/6-1>JFinal Enjoy</a> 模板引擎实现。
 *
 * @author michael
 */
@Slf4j
public class EnjoyTemplate implements ITemplate {

    private static final String VAR_ENGINE_NAME = "mybatis-flex";
    private final Engine engine;

    public EnjoyTemplate() {
        Engine varEngine = Engine.use(VAR_ENGINE_NAME);
        if (varEngine == null) {
            synchronized (EnjoyTemplate.class) {
                varEngine = Engine.use(VAR_ENGINE_NAME);
                if (varEngine == null) {
                    varEngine = Engine.create(VAR_ENGINE_NAME, e -> {
                        // 给模版添加静态方法
                        e.addSharedStaticMethod(StringUtil.class);
                        e.setStaticMethodExpression(true);
                        e.setSourceFactory(new FileAndClassPathSourceFactory());
                    });
                    // 以下配置将支持 user.girl 表达式去调用 user 对象的 boolean isGirl() 方法
                    Engine.addFieldGetterToFirst(new FieldGetters.IsMethodFieldGetter());
                }
            }
        }
        this.engine = varEngine;
    }


    @Override
    public void generate(Map<String, Object> params, String templateFilePath, File generateFile) {
        if (!generateFile.getParentFile().exists() && !generateFile.getParentFile().mkdirs()) {
            throw new IllegalStateException("Can not mkdirs by dir: " + generateFile.getParentFile());
        }
        // 开始生成文件
        try (FileOutputStream fileOutputStream = new FileOutputStream(generateFile)) {
            engine.getTemplate(templateFilePath).render(params, fileOutputStream);
        } catch (Exception e) {
            log.error("Generate file {} error:", generateFile.getAbsolutePath(), e);
        }
    }

    @Override
    public void generateByContent(Map<String, Object> params, String content, File generateFile) {
        if (!generateFile.getParentFile().exists() && !generateFile.getParentFile().mkdirs()) {
            throw new IllegalStateException("Can not mkdirs by dir: " + generateFile.getParentFile());
        }
        // 开始生成文件
        try (FileOutputStream fileOutputStream = new FileOutputStream(generateFile)) {
            engine.getTemplateByString(content).render(params, fileOutputStream);
        } catch (Exception e) {
            log.error("Generate file {} error:", generateFile.getAbsolutePath(), e);
        }
    }

    @Override
    public String previewByFile(Map<String, Object> params, String templateFilePath) {
        return engine.getTemplate(templateFilePath).renderToString(params);
    }

    @Override
    public String previewByContent(Map<String, Object> params, String content) {
        return engine.getTemplateByString(content).renderToString(params);
    }

    /**
     * 从文件或者类路径读取模板。
     *
     * @author 王帅
     */
    public static class FileAndClassPathSourceFactory implements ISourceFactory {

        @Override
        public ISource getSource(String baseTemplatePath, String fileName, String encoding) {
            // 先从文件寻找资源，找不到再从类路径寻找资源
            if (new File(fileName).exists()) {
                return new FileSource(baseTemplatePath, fileName, encoding);
            }
            return new ClassPathSource(baseTemplatePath, fileName, encoding);
        }

    }

}
