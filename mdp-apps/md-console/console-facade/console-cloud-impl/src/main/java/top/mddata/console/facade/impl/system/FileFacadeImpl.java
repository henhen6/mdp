package top.mddata.console.facade.impl.system;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.base.base.R;
import top.mddata.console.dto.system.CopyFilesDto;
import top.mddata.console.dto.system.RelateFilesToBizDto;
import top.mddata.console.facade.api.system.FileApi;
import top.mddata.console.facade.system.FileFacade;

import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FileFacadeImpl implements FileFacade {
    private final FileApi fileApi;

    @Override
    public void relateFilesToBiz(RelateFilesToBizDto relateFilesToBizDto) {
        fileApi.relateFilesToBiz(relateFilesToBizDto);
    }

    @Override
    public void releaseFilesByBiz(String objectType, Collection<Long> objectIds) {
        fileApi.releaseFilesByBiz(RelateFilesToBizDto.builder()
                .objectType(objectType).objectIds(new ArrayList<>(objectIds)).build());
    }

    @Override
    public Boolean copyFile(CopyFilesDto copyFilesDto) {
        R<Boolean> result = fileApi.copyFile(copyFilesDto);
        return result.getIsSuccess() ? result.getData() : false;
    }
}
