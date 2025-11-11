package com.gitee.sop.support.dto;

import lombok.Setter;

import java.io.Serializable;

/**
 * @author 六如
 */
@Setter
public class CommonFileData implements FileData, Serializable {
    private static final long serialVersionUID = -7737314661756983260L;

    /**
     * 上传文件表单名称
     */
    private String name;

    /**
     * 原始文件名称,可能为null
     */
    private String originalFilename;

    /**
     * 文件的contentType,可能为null
     */
    private String contentType;

    /**
     * 文件内容
     */
    private byte[] data;


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return data == null || data.length == 0;
    }

    @Override
    public long getSize() {
        return data == null ? 0 : data.length;
    }

    @Override
    public byte[] getBytes() {
        return data;
    }
}
