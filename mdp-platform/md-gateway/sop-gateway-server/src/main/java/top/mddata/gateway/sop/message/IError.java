package top.mddata.gateway.sop.message;

/**
 * 定义错误返回
 * code（返回码）
 * msg（返回码描述）
 * sub_code（明细返回码）
 * sub_msg（明细返回码描述）
 * 解决方案
 * @author 六如
 */
public interface IError {
    /**
     * 获取网关状态码
     *
     * @return 返回状态码
     */
    String getCode();

    /**
     * 获取网关错误信息
     *
     * @return 返回错误信息
     */
    String getMsg();

    /**
     * sub_code（明细返回码）
     * @return sub_code（明细返回码）
     */
    String getSubCode();

    /**
     * sub_msg（明细返回码描述）
     * @return sub_msg（明细返回码描述）
     */
    String getSubMsg();

    /**
     * 解决方案
     * @return 解决方案
     */
    String getSolution();


}
