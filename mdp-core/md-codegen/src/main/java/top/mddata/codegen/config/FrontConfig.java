/*
 *  Copyright (c) 2024.  flash-sloth (244387066@qq.com).
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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import top.mddata.codegen.config.front.ButtonConfig;

import java.io.Serial;
import java.util.List;

/**
 * 前端配置。
 *
 * @author henhen6
 * @since 2024年06月25日14:30:32
 */
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class FrontConfig extends BaseConfig {

    @Serial
    private static final long serialVersionUID = -6790274333595436008L;

    /** index页面的按钮。 */
    private List<ButtonConfig> buttonConfigList;
    /**
     * 表单打开方式。
     */
    private OpenMode openMode = OpenMode.MODAL;
    /**
     * 布局方式。
     */
    private Layout layout = Layout.SIMPLE;
    /**
     * 是否启用国际化。
     */
    private Boolean i18n = true;
    /**
     * 页面缓存。
     */
    private Boolean keepAlive = true;
    /**
     * 复选框。
     */
    private Boolean checkbox = true;

    /**
     * 单选框。
     */
    private Boolean radio = false;

//    public List<String> buildIndexTsxImports(GlobalConfig globalConfig, Table table) {
//        Set<String> imports = new HashSet<>();
//        PackageConfig packageConfig = globalConfig.getPackageConfig();
//
//        imports.add(StrUtil.format("import type { {}Type } from '#/api/{}/{}/{}/model';", table.buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(table.buildEntityClassName())));
//        imports.add(StrUtil.format("import { {}Api } from '#/api/{}/{}/{}/api';", table.buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(table.buildEntityClassName())));
//
//        return imports.stream().filter(Objects::nonNull).sorted(Comparator.naturalOrder()).toList();
//    }

//    public List<String> buildFormTsxImports(GlobalConfig globalConfig, Table table) {
//        Set<String> imports = new HashSet<>();
//        PackageConfig packageConfig = globalConfig.getPackageConfig();
//
//        imports.add(StrUtil.format("import type { {}Type } from '#/api/{}/{}/{}/model';", table.buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(table.buildEntityClassName())));
//        imports.add(StrUtil.format("import { {}Api, {}Config } from '#/api/{}/{}/{}/api';", table.buildEntityClassName(), table.buildEntityClassName(), packageConfig.getSubSystem(), packageConfig.getModule(), StringUtil.firstCharToLowerCase(table.buildEntityClassName())));
//
//        return imports.stream().filter(Objects::nonNull).sorted(Comparator.naturalOrder()).toList();
//    }

    public enum OpenMode {
        /** 弹窗、抽屉、新窗口 */
        MODAL, DRAWER, WINDOW;
    }

    public enum Layout {
        /** 单表、树、主从 */
        SIMPLE, TREE, SLAVE;
    }

}
