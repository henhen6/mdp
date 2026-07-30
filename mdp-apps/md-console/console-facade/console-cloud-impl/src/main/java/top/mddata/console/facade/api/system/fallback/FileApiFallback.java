package top.mddata.console.facade.api.system.fallback;

import org.springframework.stereotype.Component;
import top.mddata.base.base.R;
import top.mddata.console.dto.system.CopyFilesDto;
import top.mddata.console.dto.system.RelateFilesToBizDto;
import top.mddata.console.facade.api.system.FileApi;

/**
 *
 * @author henhen
 * @since 2026/5/10 23:20
 */
@Component
public class FileApiFallback implements FileApi {
    @Override
    public R<Long> relateFilesToBiz(RelateFilesToBizDto relateFilesToBizDto) {
        return R.timeout();
    }

    @Override
    public R<Boolean> copyFile(CopyFilesDto copyFilesDto) {
        return R.timeout();
    }
}
