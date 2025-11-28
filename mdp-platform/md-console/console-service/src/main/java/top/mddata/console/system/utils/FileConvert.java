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
    @Retention(RetentionPolicy.SOURCE)
    @interface ExtToFileType {
    }

    @ExtToFileType
    default Integer extToFileType(String ext) {
        return FileTypeEnum.getByExtension(ext).getCode();
    }

    @Mapping(target = "metadata", qualifiedByName = PARSE_MAP)
    @Mapping(target = "userMetadata", qualifiedByName = PARSE_MAP)
    @Mapping(target = "thMetadata", qualifiedByName = PARSE_MAP)
    @Mapping(target = "thUserMetadata", qualifiedByName = PARSE_MAP)
    @Mapping(target = "hashInfo", expression = "java(parseObject(file.getHashInfo(), org.dromara.x.file.storage.core.hash.HashInfo.class))")
    @Mapping(target = "attr", expression = "java(parseObject(file.getAttr(), cn.hutool.core.lang.Dict.class))")
    @Mapping(source = "fileSize", target = "size")
    @Mapping(target = "fileAcl", ignore = true)
    @Mapping(target = "thFileAcl", ignore = true)
    FileInfo toTarget(File file);


    @Mapping(source = "ext", target = "fileType", qualifiedBy = ExtToFileType.class)
    @Mapping(target = "metadata", qualifiedByName = TO_JSON_STRING)
    @Mapping(target = "userMetadata", qualifiedByName = TO_JSON_STRING)
    @Mapping(target = "thMetadata", qualifiedByName = TO_JSON_STRING)
    @Mapping(target = "thUserMetadata", qualifiedByName = TO_JSON_STRING)
    @Mapping(target = "hashInfo", qualifiedByName = TO_JSON_STRING)
    @Mapping(target = "attr", qualifiedByName = TO_JSON_STRING)
    @Mapping(source = "size", target = "fileSize")
    @Mapping(target = "fileAcl", ignore = true)
    @Mapping(target = "thFileAcl", ignore = true)
    @Mapping(target = "id", ignore = true)
    File toSource(FileInfo fileInfo);


}
