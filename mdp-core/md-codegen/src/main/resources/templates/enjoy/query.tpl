#set(withLombok = queryConfig.getWithLombok())
#set(withSwagger = queryConfig.getWithSwagger())
#set(swaggerVersion = queryConfig.getSwaggerVersion())
#set(jdkVersion = queryConfig.getJdkVersion())
package #(queryPackageName);

#for(importClass : queryConfig.buildImports(table))
import #(importClass);
#end

#if(jdkVersion >= 14)
import java.io.Serial;
#end

#if(withSwagger && swaggerVersion.getName() == "FOX")
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
#end
#if(withSwagger && swaggerVersion.getName() == "DOC")
import io.swagger.v3.oas.annotations.media.Schema;
#end
#if(withLombok)
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
#if(queryConfig.getSuperClass())
import lombok.EqualsAndHashCode;
#end
#end

/**
 * #(table.getComment()) Query类（查询方法入参）。
 *
 * @author #(javadocConfig.getAuthor())
 * @since #(javadocConfig.getSince())
 */
#if(withLombok)
@Accessors(chain = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
#if(queryConfig.getSuperClass())
@EqualsAndHashCode(callSuper = true)
#end
#end
#if(withSwagger && swaggerVersion.getName() == "FOX")
@ApiModel("#(table.getSwaggerComment())")
#end
#if(withSwagger && swaggerVersion.getName() == "DOC")
@Schema(description = "#(table.getSwaggerComment())Query")
#end
public class #(queryClassName)#(queryConfig.buildExtends(globalConfig))#(queryConfig.buildImplements(globalConfig)) {

    #if(jdkVersion >= 14)
    @Serial
    #end
    private static final long serialVersionUID = 1L;

#for(column : table.allColumns)
    #set(comment = javadocConfig.formatColumnComment(column.comment))
    #if(hasText(comment))
    /**
     * #(comment)
     */
    #end
    #if(withSwagger && swaggerVersion.getName() == "FOX")
    @ApiModelProperty("#(column.getSwaggerComment())")
    #end
    #if(withSwagger && swaggerVersion.getName() == "DOC")
    @Schema(description = "#(column.getSwaggerComment())")
    #end
    private #(column.propertySimpleType) #(column.property)#if(hasText(column.propertyDefaultValue)) = #(column.propertyDefaultValue)#end;

#end
#if(!withLombok)

    #for(column: table.allColumns)
    public #(column.propertySimpleType) #(column.getterMethod())() {
        return #(column.property);
    }

    public #(queryClassName) #(column.setterMethod())(#(column.propertySimpleType) #(column.property)) {
        this.#(column.property) = #(column.property);
        return this;
    }

    #end
#end}
