#set(withLombok = voConfig.getWithLombok())
#set(withSwagger = voConfig.getWithSwagger())
#set(swaggerVersion = voConfig.getSwaggerVersion())
#set(jdkVersion = voConfig.getJdkVersion())
package #(voPackageName);

#for(importClass : voConfig.buildImports(table))
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
#if(voConfig.getSuperClass())
import lombok.EqualsAndHashCode;
#end
#end

/**
 * #(table.getComment()) VO类（通常用作Controller出参）。
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
#if(voConfig.getSuperClass())
@EqualsAndHashCode(callSuper = true)
#end
#end
#if(withSwagger && swaggerVersion.getName() == "FOX")
@ApiModel("#(table.getSwaggerComment())")
#end
#if(withSwagger && swaggerVersion.getName() == "DOC")
@Schema(description = "#(table.getSwaggerComment())")
#end
#(table.buildTableAnnotation())
public class #(voClassName)#(voConfig.buildExtends(globalConfig, table))#(voConfig.buildImplements(globalConfig)) {

    #if(jdkVersion >= 14)
    @Serial
    #end
    private static final long serialVersionUID = 1L;

#(voConfig.buildEchoMap(globalConfig))

#for(column : table.allColumns)
    #set(comment = javadocConfig.formatColumnComment(column.comment))
    #if(hasText(comment))
    /**
     * #(comment)
     */
    #end
    #set(annotations = column.buildAnnotations())
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

    public #(voClassName) #(column.setterMethod())(#(column.propertySimpleType) #(column.property)) {
        this.#(column.property) = #(column.property);
        return this;
    }

    #end
#end}
