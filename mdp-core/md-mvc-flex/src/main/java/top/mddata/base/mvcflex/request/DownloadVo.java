package top.mddata.base.mvcflex.request;

import lombok.Builder;
import lombok.Data;

/**
 * @author henhen6
 * @version v1.0
 * @since 2022/6/14 8:49 PM
 * @create [2022/6/14 8:49 PM ] [henhen6] [初始创建]
 */
@Data
@Builder
public class DownloadVo {
    private byte[] data;
    private String fileName;
}
