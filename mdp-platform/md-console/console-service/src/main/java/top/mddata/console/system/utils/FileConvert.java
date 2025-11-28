package top.mddata.console.system.utils;

import org.dromara.x.file.storage.core.FileInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Qualifier;
import org.mapstruct.factory.Mappers;
import top.mddata.base.mapstruct.MapStructMethod;
import top.mddata.console.system.entity.File;
import top.mddata.console.system.enumeration.FileTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 文件对象转换
 * @author henhen6
 * @since 2025/11/27 20:51
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileConvert extends MapStructMethod {
    FileConvert INSTANCE = Mappers.getMapper(FileConvert.class);

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    @interface ExtToFileType {
    }

    @ExtToFileType
    default Integer extToFileType(String ext) {
        return FileTypeEnum.getByExtension(ext).getCode();
    }

    @Mapping(target = "metadata", qualifiedBy = ParseMap.class)
    @Mapping(target = "userMetadata", qualifiedBy = ParseMap.class)
    @Mapping(target = "thMetadata", qualifiedBy = ParseMap.class)
    @Mapping(target = "thUserMetadata", qualifiedBy = ParseMap.class)
    @Mapping(target = "hashInfo", expression = "java(parseObject(file.getHashInfo(), org.dromara.x.file.storage.core.hash.HashInfo.class))")
    @Mapping(target = "attr", expression = "java(parseObject(file.getAttr(), cn.hutool.core.lang.Dict.class))")
    @Mapping(source = "fileSize", target = "size")
    @Mapping(target = "fileAcl", ignore = true)
    @Mapping(target = "thFileAcl", ignore = true)
    FileInfo toTarget(File file);


    @Mapping(source = "ext", target = "fileType", qualifiedBy = ExtToFileType.class)
    @Mapping(target = "metadata", qualifiedBy = ToJson.class)
    @Mapping(target = "userMetadata", qualifiedBy = ToJson.class)
    @Mapping(target = "thMetadata", qualifiedBy = ToJson.class)
    @Mapping(target = "thUserMetadata", qualifiedBy = ToJson.class)
    @Mapping(target = "hashInfo", qualifiedBy = ToJson.class)
    @Mapping(target = "attr", qualifiedBy = ToJson.class)
    @Mapping(source = "size", target = "fileSize")
    @Mapping(target = "fileAcl", ignore = true)
    @Mapping(target = "thFileAcl", ignore = true)
    @Mapping(target = "id", ignore = true)
    File toSource(FileInfo fileInfo);


}
