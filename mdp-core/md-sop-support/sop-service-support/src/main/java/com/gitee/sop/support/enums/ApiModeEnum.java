package com.gitee.sop.support.enums;

import top.mddata.base.interfaces.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.dubbo.remoting.http12.rest.Schema;

/**
 * 接口模式
 * [1-open接口 2-Restful模式]
 *
 * @author 六如
 */
@Getter
@AllArgsConstructor
@Schema(description = "API模式枚举")
public enum ApiModeEnum implements BaseEnum<Integer> {

    OPEN(1, "Open模式"),
    RESTFUL(2, "Restful模式");

    private final Integer code;

    private final String desc;
}
