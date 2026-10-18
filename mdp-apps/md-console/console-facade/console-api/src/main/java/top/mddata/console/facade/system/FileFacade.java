package top.mddata.console.facade.system;

import top.mddata.console.dto.system.CopyFilesDto;
import top.mddata.console.dto.system.RelateFilesToBizDto;

import java.util.Collection;

/**
 * 业务文件接口
 */
public interface FileFacade {
    /**
     * 关联文件到业务 （新增、修改时调用）
     *
     * @param relateFilesToBizDto 参数
     */
    void relateFilesToBiz(RelateFilesToBizDto relateFilesToBizDto);

    /**
     * 释放业务关联的全部附件 （删除业务时调用）
     *
     * @param objectType 业务类型
     * @param objectIds  业务id集合，空集合视为无操作
     */
    void releaseFilesByBiz(String objectType, Collection<Long> objectIds);

    /**
     * 复制文件
     * @param copyFilesDto 复制参数
     */
    Boolean copyFile(CopyFilesDto copyFilesDto);
}
