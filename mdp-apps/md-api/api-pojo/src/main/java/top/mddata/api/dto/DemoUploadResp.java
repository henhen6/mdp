package top.mddata.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * demo 文件上传响应。
 *
 * @author henhen
 */
@Accessors(chain = true)
@Data
public class DemoUploadResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 原始文件名 */
    private String fileName;

    /** 文件大小（字节） */
    private Integer size;
}
