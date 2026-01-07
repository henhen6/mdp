package top.mddata.sdk.core.common;

/**
 * 返回固定的
 * {
 *     "result": {
 *         "code": "20000",
 *         "msg": "Service Currently Unavailable",
 *         "sub_code": "isp.unknown-error",
 *         "sub_msg": "系统繁忙"
 *     },
 *     "sign": "ERITJKEIJKJHKKKKKKKHJEREEEEEEEEEEE"
 * }
 * @author 六如
 */
public class CustomDataNameBuilder implements DataNameBuilder {
    private String dataName = "data";

    public CustomDataNameBuilder() {
    }

    public CustomDataNameBuilder(String dataName) {
        this.dataName = dataName;
    }

    @Override
    public String build(String method) {
        return dataName;
    }
}
