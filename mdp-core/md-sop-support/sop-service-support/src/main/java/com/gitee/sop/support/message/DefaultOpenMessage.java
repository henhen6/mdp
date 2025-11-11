package com.gitee.sop.support.message;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 六如
 */
@Data
@AllArgsConstructor
public class DefaultOpenMessage implements OpenMessage {
    private String code;
    private String msg;
    private String solution;
}
