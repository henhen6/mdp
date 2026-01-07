package top.mddata.sdk.core.common;

import okhttp3.Headers;

/**
 * @author 六如
 */
public class FileResult {
    private byte[] fileData;

    private Headers headers;

    public byte[] getFileData() {
        return fileData;
    }

    public FileResult setFileData(byte[] fileData) {
        this.fileData = fileData;
        return this;
    }

    public Headers getHeaders() {
        return headers;
    }

    public FileResult setHeaders(Headers headers) {
        this.headers = headers;
        return this;
    }
}
