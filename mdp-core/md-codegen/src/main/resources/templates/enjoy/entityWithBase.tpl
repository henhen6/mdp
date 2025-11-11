package #(entityPackageName);
#set(withLombok = entityConfig.getWithLombok())
#set(withSwagger = entityConfig.getWithSwagger())
#set(swaggerVersion = entityConfig.getSwaggerVersion())
#set(withActiveRecord = entityConfig.getWithActiveRecord())
#set(jdkVersion = entityConfig.getJdkVersion())

#for(importClass : table.buildImports(false))
import #(importClass);
#end
import #(baseClassPackage).#(baseClassName);
#if(withActiveRecord)
import com.mybatisflex.core.activerecord.Model;
#end
#if(withSwagger && swaggerVersion.getName() == "FOX")
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
#end
#if(withSwagger && swaggerVersion.getName() == "DOC")
import io.swagger.v3.oas.annotations.media.Schema;
#end
#if(withLombok)
#if(withActiveRecord)
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
#else
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
#end
#end

#if(!isBase)
/**
 * #(entityConfig.getDescription() ?? (table.getComment() + "实体类。"))
 * DO类：数据对象，可以在关联查询时，再次添加字段，重新生成代码时，忽略此文件。
 *
 * @author #(javadocConfig.getAuthor())
 * @since #(javadocConfig.getSince())
 */
#end
#if(withLombok)
#if(withActiveRecord)
@Accessors(chain = true)
@Data(staticConstructor = "create")
@EqualsAndHashCode(callSuper = true)
#else
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
#end
#end
#if(withSwagger && swaggerVersion.getName() == "FOX")
@ApiModel("#(table.getSwaggerComment())")
#end
#if(withSwagger && swaggerVersion.getName() == "DOC")
@Schema(description = "#(table.getSwaggerComment())")
#end
#if(!isBase)
#(table.buildTableAnnotation())
#end
public class #(entityClassName) extends #(table.buildTreeExtends(isBase, table)) {
}
