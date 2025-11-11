package com.gitee.sop.support.dto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author 六如
 */
public interface FileData {

    /**
     * 返回表单字段名
     */
    String getName();

    /**
     * 返回文件原始名称.
     */
    String getOriginalFilename();

    /**
     * 返回文件Content type,没有指定返回null
     */
    String getContentType();

    /**
     * 文件内容是否为空
     */
    boolean isEmpty();

    /**
     * 返回文件内容大小单位byte,没有返回0
     */
    long getSize();

    /**
     * 返回文字字节内容
     */
    byte[] getBytes();

    /**
     * 返回文件流
     */
    default InputStream getInputStream() {
        return new ByteArrayInputStream(getBytes());
    }

}
