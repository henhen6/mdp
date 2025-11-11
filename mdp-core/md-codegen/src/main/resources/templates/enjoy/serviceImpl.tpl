#set(isCache = serviceImplConfig.cache)
#set(primaryKey = table.getPrimaryKey().getProperty())
#set(entityClassName = table.buildEntityClassName())
package #(packageConfig.serviceImplPackage);

import #(serviceImplConfig.buildSuperClassImport());
import #(packageConfig.entityPackage).#(table.buildEntityClassName());
import #(packageConfig.mapperPackage).#(table.buildMapperClassName());
import #(packageConfig.servicePackage).#(table.buildServiceClassName());
import org.springframework.stereotype.Service;
#for(importClass : table.buildServiceImplImports())
import #(importClass);
#end

/**
 * #(table.getComment()) 服务层实现。
 *
 * @author #(javadocConfig.getAuthor())
 * @since #(javadocConfig.getSince())
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class #(serviceImplClassName) extends #(serviceImplConfig.buildSuperClassName())<#(table.buildMapperClassName()), #(table.buildEntityClassName())> implements #(table.buildServiceClassName()) {

#if(isCache)
    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return () -> "#(entityClassName)";
    }
#end
}
