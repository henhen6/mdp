package top.mddata.open.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import top.mddata.base.mapstruct.BaseMapStruct;
import top.mddata.open.admin.dto.AppApplyDto;
import top.mddata.open.admin.entity.AppApply;

import java.util.List;

/**
 *
 * @author henhen6
 * @since 2025/12/9 22:41
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AppApplyConvert extends BaseMapStruct<AppApply, AppApplyDto> {

    @Override
    @Mapping(target = AppApplyDto.Fields.credentialFile, ignore = true)
    AppApplyDto toTarget(AppApply appApply);

    @Override
    @Mapping(target = AppApplyDto.Fields.credentialFile, ignore = true)
    List<AppApplyDto> toTargetList(List<AppApply> aList);

    @Override
    @Mapping(target = AppApplyDto.Fields.credentialFile, ignore = true)
    void copySourceProperties(AppApply appApply, @MappingTarget AppApplyDto appApplyDto);

    @Override
    @Mapping(target = AppApplyDto.Fields.credentialFile, ignore = true)
    void copySourceProperties(List<AppApply> aList, @MappingTarget List<AppApplyDto> bList);

    @Override
    @Mapping(target = AppApply.Fields.credentialFile, ignore = true)
    AppApply toSource(AppApplyDto appApplyDto);

    @Override
    @Mapping(target = AppApply.Fields.credentialFile, ignore = true)
    List<AppApply> toSource(List<AppApplyDto> bList);

    @Override
    @Mapping(target = AppApply.Fields.credentialFile, ignore = true)
    void copyTargetProperties(AppApplyDto b, @MappingTarget AppApply a);

    @Override
    @Mapping(target = AppApply.Fields.credentialFile, ignore = true)
    void copyTargetProperties(List<AppApplyDto> bList, @MappingTarget List<AppApply> aList);
}
