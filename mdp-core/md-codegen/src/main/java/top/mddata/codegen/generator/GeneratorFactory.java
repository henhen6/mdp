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

import top.mddata.codegen.constant.GenTypeConst;
import top.mddata.codegen.constant.GenTypeEnum;
import top.mddata.codegen.generator.impl.ControllerGenerator;
import top.mddata.codegen.generator.impl.DtoGenerator;
import top.mddata.codegen.generator.impl.EntityBaseGenerator;
import top.mddata.codegen.generator.impl.EntityGenerator;
import top.mddata.codegen.generator.impl.MapperGenerator;
import top.mddata.codegen.generator.impl.QueryGenerator;
import top.mddata.codegen.generator.impl.ServiceGenerator;
import top.mddata.codegen.generator.impl.ServiceImplGenerator;
import top.mddata.codegen.generator.impl.TableDefGenerator;
import top.mddata.codegen.generator.impl.VoGenerator;
import top.mddata.codegen.generator.impl.front.ApiTsGenerator;
import top.mddata.codegen.generator.impl.front.LangEnJsonGenerator;
import top.mddata.codegen.generator.impl.front.LangZhJsonGenerator;
import top.mddata.codegen.generator.impl.front.ModelTsGenerator;
import top.mddata.codegen.generator.impl.front.PermTsGenerator;
import top.mddata.codegen.generator.impl.front.table.DetailTsxGenerator;
import top.mddata.codegen.generator.impl.front.table.DetailVueGenerator;
import top.mddata.codegen.generator.impl.front.table.FormTsxGenerator;
import top.mddata.codegen.generator.impl.front.table.FormVueGenerator;
import top.mddata.codegen.generator.impl.front.table.IndexTsxGenerator;
import top.mddata.codegen.generator.impl.front.table.IndexVueGenerator;
import top.mddata.codegen.generator.impl.front.table.WrapperVueGenerator;
import top.mddata.codegen.generator.impl.front.tree.MoveTsxGenerator;
import top.mddata.codegen.generator.impl.front.tree.MoveVueGenerator;
import top.mddata.codegen.generator.impl.front.tree.TreeFormTsxGenerator;
import top.mddata.codegen.generator.impl.front.tree.TreeFormVueGenerator;
import top.mddata.codegen.generator.impl.front.tree.TreeIndexTsxGenerator;
import top.mddata.codegen.generator.impl.front.tree.TreeIndexVueGenerator;
import top.mddata.codegen.generator.impl.front.tree.TreeTsxGenerator;
import top.mddata.codegen.generator.impl.front.tree.TreeVueGenerator;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 代码生成器工厂，用于创建各种类型文件的生成。
 *
 * @author henhen6
 * @see GenTypeConst
 */
public class GeneratorFactory {

    private static final Map<GenTypeEnum, IGenerator> GENERATORS = new LinkedHashMap<>();

    static {
        registerGenerator(GenTypeEnum.ENTITY, new EntityGenerator());
        registerGenerator(GenTypeEnum.ENTITY_BASE, new EntityBaseGenerator());
        registerGenerator(GenTypeEnum.DTO, new DtoGenerator());
        registerGenerator(GenTypeEnum.QUERY, new QueryGenerator());
        registerGenerator(GenTypeEnum.VO, new VoGenerator());
        registerGenerator(GenTypeEnum.MAPPER, new MapperGenerator());
        registerGenerator(GenTypeEnum.SERVICE, new ServiceGenerator());
        registerGenerator(GenTypeEnum.SERVICE_IMPL, new ServiceImplGenerator());
        registerGenerator(GenTypeEnum.CONTROLLER, new ControllerGenerator());
        registerGenerator(GenTypeEnum.TABLE_DEF, new TableDefGenerator());
//        registerGenerator(GenTypeEnum.MAPPER_XML, new MapperXmlGenerator());
//        registerGenerator(GenTypeConst.PACKAGE_INFO, new PackageInfoGenerator());
        registerGenerator(GenTypeEnum.INDEX_TSX, new IndexTsxGenerator());
        registerGenerator(GenTypeEnum.FORM_TSX, new FormTsxGenerator());
        registerGenerator(GenTypeEnum.DETAIL_VUE, new DetailVueGenerator());
        registerGenerator(GenTypeEnum.DETAIL_TSX, new DetailTsxGenerator());
        registerGenerator(GenTypeEnum.FORM_VUE, new FormVueGenerator());
        registerGenerator(GenTypeEnum.INDEX_VUE, new IndexVueGenerator());
        registerGenerator(GenTypeEnum.WRAPPER_VUE, new WrapperVueGenerator());

        registerGenerator(GenTypeEnum.TREE_INDEX_TSX, new TreeIndexTsxGenerator());
        registerGenerator(GenTypeEnum.TREE_FORM_TSX, new TreeFormTsxGenerator());
        registerGenerator(GenTypeEnum.TREE_TREE_TSX, new TreeTsxGenerator());
        registerGenerator(GenTypeEnum.TREE_MOVE_TSX, new MoveTsxGenerator());

        registerGenerator(GenTypeEnum.TREE_INDEX_VUE, new TreeIndexVueGenerator());
        registerGenerator(GenTypeEnum.TREE_FORM_VUE, new TreeFormVueGenerator());
        registerGenerator(GenTypeEnum.TREE_TREE_VUE, new TreeVueGenerator());
        registerGenerator(GenTypeEnum.TREE_MOVE_VUE, new MoveVueGenerator());

        registerGenerator(GenTypeEnum.MODEL_TS, new ModelTsGenerator());
        registerGenerator(GenTypeEnum.API_TS, new ApiTsGenerator());
        registerGenerator(GenTypeEnum.LANG_EN, new LangEnJsonGenerator());
        registerGenerator(GenTypeEnum.LANG_ZH, new LangZhJsonGenerator());
        registerGenerator(GenTypeEnum.PERM_TS, new PermTsGenerator());
    }

    private GeneratorFactory() {
    }

    /**
     * 获取指定类型文件的生成器。
     *
     * @param genType 生成类型
     * @return 该类型的文件生成器
     */
    public static IGenerator getGenerator(GenTypeEnum genType) {
        return GENERATORS.get(genType);
    }

    /**
     * 获取所有的文件生成器。
     *
     * @return 所有的文件生成器
     */
    public static Collection<IGenerator> getGenerators() {
        return GENERATORS.values();
    }

    /**
     * 注册文件生成器。
     *
     * @param name      生成器名称
     * @param generator 生成器
     */
    public static void registerGenerator(GenTypeEnum name, IGenerator generator) {
        GENERATORS.put(name, generator);
    }

}
