package top.mddata.gateway.sop.response;

import lombok.Data;
import top.mddata.gateway.sop.message.ErrorEnum;
import top.mddata.gateway.sop.message.IError;

import java.util.Locale;


/**
 * 默认的结果封装类.
 * <pre>
 *
 * xml返回结果:
 * <response>
 *     <code>50</code>
 *     <msg>Remote service error</msg>
 *     <sub_code>isv.invalid-parameter</sub_code>
 *     <sub_msg>参数错误</sub_msg>
 * </response>
 * 成功情况：
 * <response>
 *     <code>0</code>
 *     <msg>成功消息</msg>
 *     <data>
 *         ...返回内容
 *     </data>
 * </response>
 *
 * json返回格式：
 * {
 *  "code":"50",
 * 	"msg":"Remote service error",
 * 	"sub_code":"isv.invalid-parameter",
 * 	"sub_msg":"参数错误"
 * }
 * 成功情况：
 * {
 *  "code":"0",
 * 	"msg":"成功消息内容。。。",
 * 	"data":{
 * 	    ...返回内容
 *    }
 * }
 * </pre>
 * <p>
 * 字段说明：
 * code:网关异常码 <br>
 * msg:网关异常信息 <br>
 * sub_code:业务异常码 <br>
 * sub_msg:业务异常信息 <br>
 *
 * @author 六如
 */
@Data
public class ApiResponse implements Response {

    public static final String SUCCESS_CODE = "0";
    public static final String SUCCESS_MSG = "success";

    /**
     * 网关异常码，范围0~100 成功返回"0"
     */
    private String code = SUCCESS_CODE;

    /**
     * 网关异常信息
     */
    private String msg = "";

    /**
     * 返回对象
     */
    private Object data;

    /**
     * 业务异常码
     */
    private String sub_code = "";

    /**
     * 业务异常信息
     */
    private String sub_msg = "";

    /**
     * 解决方案
     */
    private String solution;

    public static ApiResponse success(Object data) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(SUCCESS_CODE);
        apiResponse.setMsg(SUCCESS_MSG);
        apiResponse.setData(data);
        return apiResponse;
    }


    public static ApiResponse error(ErrorEnum errorEnum, Locale locale, String subMsg) {
        IError error = errorEnum.getError(locale);
        return error(error, subMsg);
    }

    public static ApiResponse error(ErrorEnum errorEnum, Locale locale, String subCode, String subMsg, String solution) {
        IError error = errorEnum.getError(locale);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(error.getCode());
        apiResponse.setMsg(error.getMsg());
        apiResponse.setSub_code(subCode);
        apiResponse.setSub_msg(subMsg);
        apiResponse.setSolution(solution);
        return apiResponse;
    }

    public static ApiResponse error(ErrorEnum errorEnum, Locale locale) {
        IError error = errorEnum.getError(locale);
        return error(error);
    }

    public static ApiResponse error(IError error) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSub_code(error.getSubCode());
        apiResponse.setSub_msg(error.getSubMsg());
        apiResponse.setCode(error.getCode());
        apiResponse.setMsg(error.getMsg());
        apiResponse.setSolution(error.getSolution());
        return apiResponse;
    }

    private static ApiResponse error(IError error, String subMsg) {
        ApiResponse response = error(error);
        response.setSub_msg(subMsg);
        return response;
    }
}
