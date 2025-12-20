package top.mddata.console.system.convert;

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

    @ExtToFileType
    default Integer extToFileType(String ext) {
        return FileTypeEnum.getByExtension(ext).getCode();
    }

    @Mapping(target = File.Fields.metadata, qualifiedBy = ParseMap.class)
    @Mapping(target = File.Fields.userMetadata, qualifiedBy = ParseMap.class)
    @Mapping(target = File.Fields.thMetadata, qualifiedBy = ParseMap.class)
    @Mapping(target = File.Fields.thUserMetadata, qualifiedBy = ParseMap.class)
    @Mapping(target = File.Fields.hashInfo, expression = "java(parseObject(file.getHashInfo(), org.dromara.x.file.storage.core.hash.HashInfo.class))")
    @Mapping(target = File.Fields.attr, expression = "java(parseObject(file.getAttr(), cn.hutool.core.lang.Dict.class))")
    @Mapping(source = File.Fields.fileSize, target = "size")
    @Mapping(target = File.Fields.fileAcl, ignore = true)
    @Mapping(target = File.Fields.thFileAcl, ignore = true)
    FileInfo toTarget(File file);

    @Mapping(source = File.Fields.ext, target = "fileType", qualifiedBy = ExtToFileType.class)
    @Mapping(target = File.Fields.metadata, qualifiedBy = ToJson.class)
    @Mapping(target = File.Fields.userMetadata, qualifiedBy = ToJson.class)
    @Mapping(target = File.Fields.thMetadata, qualifiedBy = ToJson.class)
    @Mapping(target = File.Fields.thUserMetadata, qualifiedBy = ToJson.class)
    @Mapping(target = File.Fields.hashInfo, qualifiedBy = ToJson.class)
    @Mapping(target = File.Fields.attr, qualifiedBy = ToJson.class)
    @Mapping(source = "size", target = File.Fields.fileSize)
    @Mapping(target = File.Fields.fileAcl, ignore = true)
    @Mapping(target = File.Fields.thFileAcl, ignore = true)
    @Mapping(target = File.ID_FIELD, ignore = true)
    File toSource(FileInfo fileInfo);


    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    @interface ExtToFileType {
    }


}
