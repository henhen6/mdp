#set(withLombok = dtoConfig.getWithLombok())
#set(withSwagger = dtoConfig.getWithSwagger())
#set(swaggerVersion = dtoConfig.getSwaggerVersion())
#set(jdkVersion = dtoConfig.getJdkVersion())
package #(dtoPackageName);

#for(importClass : dtoConfig.buildImports(globalConfig, table))
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
#if(dtoConfig.getSuperClass())
import lombok.EqualsAndHashCode;
#end
#end

/**
 * #(table.getComment()) DTO（写入方法入参）。
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
#if(dtoConfig.getSuperClass())
@EqualsAndHashCode(callSuper = true)
#end
#end
#if(withSwagger && swaggerVersion.getName() == "FOX")
@ApiModel("#(table.getSwaggerComment())")
#end
#if(withSwagger && swaggerVersion.getName() == "DOC")
@Schema(description = "#(table.getSwaggerComment())Dto")
#end
public class #(dtoClassName)#(dtoConfig.buildExtends(globalConfig))#(dtoConfig.buildImplements(globalConfig)) {

    #if(jdkVersion >= 14)
    @Serial
    #end
    private static final long serialVersionUID = 1L;

#for(column : table.allColumns)
    #if(dtoConfig.getIgnoreColumns().contains(column.getName().toLowerCase()))
    #continue
    #end
    #set(comment = javadocConfig.formatColumnComment(column.comment))
    #if(hasText(comment))
    /**
     * #(comment)
     */
    #end
    #set(annotations = column.buildValidatorAnnotations())
    #if(hasText(annotations))
    #(annotations)
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

    public #(dtoClassName) #(column.setterMethod())(#(column.propertySimpleType) #(column.property)) {
        this.#(column.property) = #(column.property);
        return this;
    }

    #end
#end}
