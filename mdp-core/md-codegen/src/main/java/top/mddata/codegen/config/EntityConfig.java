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
import top.mddata.codegen.entity.Table;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.TypeVariable;
import java.util.function.Function;

/**
 * 生成 Entity 的配置。
 *
 * @author 王帅
 * @since 2023-05-15
 */
@Data
@Accessors(chain = true)
public class EntityConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = -6790274333595436008L;

    /**
     * 代码生成目录，当未配置时，使用 PackageConfig 的配置
     */
    private String sourceDir;

    /**
     * Entity 类的前缀。
     */
    private String classPrefix = "";

    /**
     * Entity 类的后缀。
     */
    private String classSuffix = "";

    /**
     * 类名
     */
    private String name;
    /**
     * swagger注释
     */
    private String description;

    /**
     * Entity 类的父类，可以自定义一些 BaseEntity 类。
     */
    private Class<?> superClass;

    /** Entity 类的父类工厂 */
    private Function<Table, Class<?>> superClassFactory;
    /** 父类泛型的类型 */
    private Class<?> genericityType;

    /**
     * 生成策略。
     */
    private GenerationStrategyEnum generationStrategy = GenerationStrategyEnum.OVERWRITE;

    /**
     * Entity 默认实现的接口。
     */
    private Class<?>[] implInterfaces = {Serializable.class};

    /**
     * Entity 是否使用 Lombok 注解。
     */
    private Boolean withLombok = true;
    private Boolean withChain = true;

    /**
     * Entity 是否使用 Swagger 注解。
     */
    private Boolean withSwagger = false;

    /**
     * Swagger 版本
     */
    private SwaggerVersion swaggerVersion = SwaggerVersion.DOC;

    /**
     * Entity 是否启用 Active Record 功能。
     */
    private Boolean withActiveRecord = false;

    /**
     * 实体类数据源。
     */
    private String dataSource;

    /**
     * 项目jdk版本
     */
    private int jdkVersion;

    /**
     * 当开启这个配置后，Entity 会生成两个类，比如 Account 表会生成 Account.java 以及 AccountBase.java
     * 这样的好处是，自动生成的 getter setter 字段等都在 Base 类里，而开发者可以在 Account.java 中添加自己的业务代码
     * 此时，当有数据库表结构发生变化，需要再次生成代码时，不会覆盖掉 Account.java 中的业务代码（只会覆盖 AccountBase 中的 Getter Setter）
     */
    private Boolean withBaseClassEnable = true;

    /**
     * Base 类的后缀
     */
    private String withBaseClassSuffix = "Base";

    /**
     * Base 类所在的包，默认情况下是在 entity 包下，添加一个 base 文件夹。
     */
    private String withBasePackage;

    /**
     * 是否支持把 comment 添加到 @column 注解里
     */
    private Boolean columnCommentEnable = false;

    /**
     * 是否总是生成 @Column 注解。
     */
    private Boolean alwaysGenColumnAnnotation = false;

    /**
     * 继承的父类是否添加泛型
     */
    private Boolean superClassGenericity = false;


    /**
     * 设置父类。
     */
    public EntityConfig setSuperClass(Class<?> superClass) {
        this.superClass = superClass;
        superClassGenericity = hasGenericity(superClass);
        return this;
    }

    public String getGenericityTypeName() {
        return genericityType != null ? genericityType.getSimpleName() : null;
    }


    private boolean hasGenericity(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        TypeVariable<? extends Class<?>>[] typeParameters = clazz.getTypeParameters();
        if (typeParameters.length > 2) {
            throw new UnsupportedOperationException("暂不支持父类泛型数量 >2 的代码生成");
        } else {
            return typeParameters.length > 0;
        }
    }


    public Class<?> getSuperClass(Table table) {
        if (superClassFactory != null) {
            return superClassFactory.apply(table);
        }
        return superClass;
    }


    /**
     * 设置实现接口。
     */
    public EntityConfig setImplInterfaces(Class<?>... implInterfaces) {
        this.implInterfaces = implInterfaces;
        return this;
    }


    /**
     * 设置是否启用 Swagger。
     */
    public EntityConfig setWithSwagger(Boolean withSwagger) {
        this.withSwagger = withSwagger;
        this.swaggerVersion = SwaggerVersion.DOC;
        return this;
    }


    /**
     * 设置 Swagger 版本
     */
    public EntityConfig setSwaggerVersion(SwaggerVersion swaggerVersion) {
        this.swaggerVersion = swaggerVersion;
        this.withSwagger = swaggerVersion != null;
        return this;
    }

    public boolean isSuperClassGenericity(Table table) {
        if (this.superClassFactory != null) {
            return hasGenericity(superClassFactory.apply(table));
        }
        return superClassGenericity;
    }


    public int superClassGenericitySize(Table table) {
        Class<?> clazz = superClassFactory.apply(table);
        TypeVariable<? extends Class<?>>[] typeParameters = clazz.getTypeParameters();
        return typeParameters.length;
    }


    /** swagger 版本 */
    @Getter
    public enum SwaggerVersion {
        /** swagger 版本 */

        FOX("FOX"),
        DOC("DOC");

        private final String name;

        SwaggerVersion(String name) {
            this.name = name;
        }
    }

}
