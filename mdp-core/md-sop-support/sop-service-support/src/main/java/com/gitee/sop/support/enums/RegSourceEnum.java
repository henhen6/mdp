package com.gitee.sop.support.enums;

import top.mddata.base.interfaces.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.dubbo.remoting.http12.rest.Schema;

/**
 * 注册来源
 * 1-系统注册,2-手动注册
 *
 * @author 六如
 */
@Getter
@AllArgsConstructor
@Schema(description = "注册来源")
public enum RegSourceEnum implements BaseEnum<Integer> {

    SYSTEM(1, "系统注册"),
    MANUAL(2, "手动注册");

    private final Integer code;

    private final String desc;
}
