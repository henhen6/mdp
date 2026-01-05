package top.mddata.gateway.sop.response;

/**
 * 没有公共返回结果
 *
 * @author 六如
 */
public class NoCommonResponse extends ApiResponse {

    public static NoCommonResponse success(Object data) {
        NoCommonResponse apiResponse = new NoCommonResponse();
        apiResponse.setCode(SUCCESS_CODE);
        apiResponse.setMsg(SUCCESS_MSG);
        apiResponse.setData(data);
        return apiResponse;
    }

    @Override
    public boolean needWrap() {
        return false;
    }
}
