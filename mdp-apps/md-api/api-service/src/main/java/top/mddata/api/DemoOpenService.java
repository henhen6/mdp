package top.mddata.api;

import com.gitee.sop.support.annotation.Open;
import com.gitee.sop.support.dto.FileData;
import top.mddata.api.dto.DemoUploadResp;

/**
 * 开放平台能力验证接口（demo）：用于验证经网关的文件上传/下载链路。
 *
 * @author henhen
 */
public interface DemoOpenService {

    /**
     * 文件上传（验证 multipart 经网关解析注入 FileData）。
     *
     * @param file 上传文件
     * @return 文件名与大小
     */
    @Open("demo.upload.more")
    DemoUploadResp uploadMore(FileData file);

    /**
     * 文件下载（验证 FileData 经网关还原为文件响应）。
     *
     * @return 固定内容的演示文件
     */
    @Open("demo.download")
    FileData download();
}
