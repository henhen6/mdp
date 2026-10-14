package top.mddata.sdk.simple.response.demo;

/**
 * demo 文件上传响应。
 *
 * @author henhen
 */
public class DemoUploadResp {
    private String fileName;
    private Integer size;

    public String getFileName() {
        return fileName;
    }

    public DemoUploadResp setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public DemoUploadResp setSize(Integer size) {
        this.size = size;
        return this;
    }
}
