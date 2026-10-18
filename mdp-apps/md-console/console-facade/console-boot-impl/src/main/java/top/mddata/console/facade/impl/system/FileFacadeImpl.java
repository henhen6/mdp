package top.mddata.console.facade.impl.system;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.console.dto.system.CopyFilesDto;
import top.mddata.console.dto.system.RelateFilesToBizDto;
import top.mddata.console.facade.system.FileFacade;
import top.mddata.console.service.system.FileService;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FileFacadeImpl implements FileFacade {
    private final FileService fileService;

    @Override
    public void relateFilesToBiz(RelateFilesToBizDto relateFilesToBizDto) {
        fileService.relateFilesToBiz(relateFilesToBizDto);
    }

    @Override
    public void releaseFilesByBiz(String objectType, Collection<Long> objectIds) {
        fileService.releaseFilesByBiz(objectType, objectIds);
    }

    @Override
    public Boolean copyFile(CopyFilesDto copyFilesDto) {
        return fileService.copyFile(copyFilesDto);
    }
}
