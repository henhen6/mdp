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

import com.mybatisflex.core.util.StringUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * 注释配置类。
 *
 * @author 王帅
 * @since 2023-05-17
 */
@Data
@Accessors(chain = true)
public class JavadocConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = -4280345489968397327L;
    /**
     * 作者。
     */
    private String author = System.getProperty("user.name");

    /**
     * 自。
     */
    private Supplier<String> since = () -> DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());

    /**
     * 表名格式化。
     */
    private UnaryOperator<String> tableCommentFormat = UnaryOperator.identity();
    /**
     * 表名Swagger注释格式化。
     */
    private UnaryOperator<String> tableSwaggerCommentFormat = UnaryOperator.identity();

    /**
     * 列名格式化。
     */
    private UnaryOperator<String> columnCommentFormat = UnaryOperator.identity();
    /**
     * 列名Swagger注释格式化。
     */
    private UnaryOperator<String> columnSwaggerCommentFormat = UnaryOperator.identity();

    /**
     * Entity 包注释。
     */
    private String entityPackage = "实体类层（Entity）软件包。";

    /**
     * VO 包注释。
     */
    private String voPackage = "VO类层（Vo：Controller层出参）软件包。";
    /**
     * DTO 包注释。
     */
    private String dtoPackage = "DTO类层（Dto：Controller层写入方法入参）软件包。";
    /**
     * Query 包注释。
     */
    private String queryPackage = "Query类层（Query：Controller层入参）软件包。";

    /**
     * Mapper 包注释。
     */
    private String mapperPackage = "映射层（Mapper）软件包。";

    /**
     * Service 包注释。
     */
    private String servicePackage = "服务层（Service）软件包。";

    /**
     * ServiceImpl 包注释。
     */
    private String serviceImplPackage = "服务层实现（ServiceImpl）软件包。";

    /**
     * Controller 包注释。
     */
    private String controllerPackage = "控制层（Controller）软件包。";

    /**
     * TableDef 包注释。
     */
    private String tableDefPackage = "表定义层（TableDef）软件包。";

    /**
     * 获取自。
     */
    public String getSince() {
        return since.get();
    }

    /**
     * 设置自。
     */
    public JavadocConfig setSince(String since) {
        this.since = () -> since;
        return this;
    }

    /**
     * 设置自。
     */
    public JavadocConfig setSince(Supplier<String> since) {
        this.since = since;
        return this;
    }

    public String formatTableComment(String comment) {
        if (StringUtil.noText(comment)) {
            return "";
        }
        return tableCommentFormat.apply(comment);
    }


    public String formatColumnComment(String comment) {
        if (StringUtil.noText(comment)) {
            return "";
        }
        return columnCommentFormat.apply(comment);
    }

    public String formatColumnSwaggerComment(String comment) {
        if (StringUtil.noText(comment)) {
            return "";
        }
        return columnSwaggerCommentFormat.apply(comment);
    }

    public String formatTableSwaggerComment(String comment) {
        if (StringUtil.noText(comment)) {
            return "";
        }
        return tableSwaggerCommentFormat.apply(comment);
    }

}
