package com.gitee.sop.support.service;

import com.gitee.sop.support.service.dto.RegisterDTO;
import com.gitee.sop.support.service.dto.RegisterResult;

import java.util.Collection;
import java.util.Collections;

/**
 * @author 六如
 */
public interface ApiRegisterService {
    /**
     * 接口注册
     *
     * @param registerDTO 接口信息
     */
    default RegisterResult register(RegisterDTO registerDTO) {
        return register(Collections.singletonList(registerDTO));
    }

    /**
     * 接口注册批量
     *
     * @param registerDTOS 接口信息
     */
    RegisterResult register(Collection<RegisterDTO> registerDTOS);
}
