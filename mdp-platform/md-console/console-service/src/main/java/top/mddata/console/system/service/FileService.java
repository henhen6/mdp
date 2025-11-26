package top.mddata.console.system.service;

import org.springframework.web.multipart.MultipartFile;
import top.mddata.base.mvcflex.service.SuperService;
import top.mddata.console.system.dto.CopyFilesDto;
import top.mddata.console.system.dto.FileUploadDto;
import top.mddata.console.system.dto.RelateFilesToBizDto;
import top.mddata.console.system.entity.File;
import top.mddata.console.system.vo.FileVo;

import java.util.List;
import java.util.Map;

/**
 * 文件 服务层。
 *
 * @author henhen6
 * @since 2025-11-12 16:21:25
 */
public interface FileService extends SuperService<File> {
    /**
     * 上传文件
     * @param file 文件
     * @param fileUploadDto 额外参数
     * @return 文件信息
     */
    FileVo upload(MultipartFile file, FileUploadDto fileUploadDto);

    /**
     * 复制文件
     * @param copyFilesDto 复制参数
     */
    Boolean copyFile(CopyFilesDto copyFilesDto);

    /**
     * 关联文件到业务 （新增、修改时调用）
     *
     * @param relateFilesToBizDto 参数
     */
    void relateFilesToBiz(RelateFilesToBizDto relateFilesToBizDto);

    /**
     * 根据文件id查询文件的临时访问路径
     *
     * @param ids 文件id
     * @return 文件集合
     */
    Map<Long, FileVo> findUrlByIds(List<Long> ids);

    /**
     * 根据业务类型和业务id，获取文件的访问路径
     *
     * @param objectType 业务类型
     * @param objectId   业务id
     * @return 文件集合
     */
    Map<Long, FileVo> findUrlByObject(String objectType, Long objectId);
}
