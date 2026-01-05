package top.mddata.gateway.sop.request;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 获取上传文件
 *
 * @author 六如
 */
public interface UploadContext {
    /**
     * 根据索引获取上传文件,从0开始
     *
     * @param index
     * @return 返回上传文件信息
     */
    MultipartFile getFile(int index);

    /**
     * 根据表单名获取上传文件
     *
     * @param name
     *            表单名称
     * @return 返回上传文件信息
     */
    List<MultipartFile> getFile(String name);

    /**
     * 获取所有的上传文件
     *
     * @return 返回所有的上传文件
     */
    List<MultipartFile> getAllFile();
}
