package top.mddata.gateway.sop.response;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author 六如
 */
public interface Response {

    String getCode();

    Object getData();

    @JSONField(serialize = false)
    @JsonIgnore
    default boolean needWrap() {
        return true;
    }
}
