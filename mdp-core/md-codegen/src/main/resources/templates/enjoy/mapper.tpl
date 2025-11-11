package #(packageConfig.mapperPackage);

#if(mapperConfig.getMapperAnnotation())
import org.apache.ibatis.annotations.Mapper;
#end
import org.springframework.stereotype.Repository;
import #(mapperConfig.buildSuperClassImport());
import #(packageConfig.entityPackage).#(table.buildEntityClassName());

/**
 * #(table.getComment()) 映射层。
 *
 * @author #(javadocConfig.getAuthor())
 * @since #(javadocConfig.getSince())
 */
#if(mapperConfig.getMapperAnnotation())
@Mapper
#end
@Repository
public interface #(mapperClassName) extends #(mapperConfig.buildSuperClassName())<#(table.buildEntityClassName())> {

}
