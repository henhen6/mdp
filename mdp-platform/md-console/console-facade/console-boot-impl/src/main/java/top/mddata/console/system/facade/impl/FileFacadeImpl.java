package top.mddata.console.system.facade.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.mddata.console.system.dto.RelateFilesToBizDto;
import top.mddata.console.system.facade.FileFacade;
import top.mddata.console.system.service.FileService;

@Service
@RequiredArgsConstructor
public class FileFacadeImpl implements FileFacade {
    private final FileService fileService;

    @Override
    public void relateFilesToBiz(RelateFilesToBizDto relateFilesToBizDto) {
        fileService.relateFilesToBiz(relateFilesToBizDto);
    }
}
