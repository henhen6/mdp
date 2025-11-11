package com.gitee.sop.support.service.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 六如
 */
@Data
public class RegisterDTO implements Serializable {
    private static final long serialVersionUID = 2183251167679411550L;

    /**
     * 所属应用
     */
    private String application;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 版本号
     */
    private String apiVersion;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 接口class
     */
    private String interfaceClassName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数信息
     */
    private String paramInfo;

    /**
     * 接口是否需要授权访问
     */
    private Integer isPermission;

    /**
     * 是否需要appAuthToken
     */
    private Integer isNeedToken;

    /**
     * 是否有公共响应参数
     */
    private Integer hasCommonResponse;

    /**
     * 接口模式，1-open接口，2-Restful模式
     */
    private Integer apiMode;


}
