package com.gitee.sop.support.register;

import com.gitee.sop.support.service.dto.RegisterDTO;

import java.util.List;

/**
 * @author 六如
 */
public class ApiRegException extends Exception {

    private static final long serialVersionUID = 241480223796854916L;
    private final List<RegisterDTO> registerDTOS;

    public ApiRegException(Exception e, List<RegisterDTO> registerDTOS) {
        super(e);
        this.registerDTOS = registerDTOS;
    }

    public List<RegisterDTO> getRegisterDTOS() {
        return registerDTOS;
    }
}
