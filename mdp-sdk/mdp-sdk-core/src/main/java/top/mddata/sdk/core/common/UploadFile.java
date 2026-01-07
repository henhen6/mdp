package top.mddata.sdk.core.common;


import top.mddata.sdk.core.util.FileUtil;
import top.mddata.sdk.core.util.MD5Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * 文件上传类
 * @author 六如
 */
public class UploadFile implements Serializable {
    private static final long serialVersionUID = -1100614660944996398L;
    private String name;
    private String fileName;
    private byte[] fileData;
    private String md5;

    /**
     * @param name 表单名称，不能重复
     * @param file 文件
     * @throws IOException IO异常
     */
    public UploadFile(String name, File file) throws IOException {
        this(name, file.getName(), FileUtil.toBytes(file));
    }

    /**
     * @param name 表单名称，不能重复
     * @param fileName 文件名
     * @param input 文件流
     * @throws IOException IO异常
     */
    public UploadFile(String name, String fileName, InputStream input) throws IOException {
        this(name, fileName, FileUtil.toBytes(input));
    }

    /**
     * @param name 表单名称，不能重复
     * @param fileName 文件名
     * @param fileData 文件数据
     */
    public UploadFile(String name, String fileName, byte[] fileData) {
        super();
        this.name = name;
        this.fileName = fileName;
        this.fileData = fileData;
        this.md5 = MD5Util.encrypt(fileData);
    }

    public String getName() {
        return name;
    }

    public UploadFile setName(String name) {
        this.name = name;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public UploadFile setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public UploadFile setFileData(byte[] fileData) {
        this.fileData = fileData;
        return this;
    }

    public String getMd5() {
        return md5;
    }

    public UploadFile setMd5(String md5) {
        this.md5 = md5;
        return this;
    }
}
