package #(packageConfig.servicePackage);

import #(serviceConfig.buildSuperClassImport());
import #(packageConfig.entityPackage).#(table.buildEntityClassName());

/**
 * #(table.getComment()) 服务层。
 *
 * @author #(javadocConfig.getAuthor())
 * @since #(javadocConfig.getSince())
 */
public interface #(serviceClassName) extends #(serviceConfig.buildSuperClassName())<#(table.buildEntityClassName())> {

}
