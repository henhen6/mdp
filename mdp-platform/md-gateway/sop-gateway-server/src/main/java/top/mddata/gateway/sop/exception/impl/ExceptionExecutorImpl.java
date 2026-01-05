package top.mddata.gateway.sop.exception.impl;

import org.apache.dubbo.rpc.service.GenericException;
import org.springframework.stereotype.Service;
import top.mddata.gateway.sop.exception.ApiException;
import top.mddata.gateway.sop.exception.ExceptionExecutor;
import top.mddata.gateway.sop.message.ErrorEnum;
import top.mddata.gateway.sop.request.ApiRequestContext;
import top.mddata.gateway.sop.response.ApiResponse;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author 六如
 */
@Service
public class ExceptionExecutorImpl implements ExceptionExecutor {

    private static final String CONSTRAINT_VIOLATION_EXCEPTION = "ConstraintViolationException";
    private static final String OPEN_EXCEPTION = "OpenException";
    private static final String BIZ_ERROR_REGEX = "<OPEN_ERROR>(.*?)</OPEN_ERROR>";
    private static final Pattern PATTERN = Pattern.compile(BIZ_ERROR_REGEX, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static Set<String> findErrorMsg(String text) {
        Pattern pattern = Pattern.compile("'([^']*)'");
        Matcher matcher = pattern.matcher(text);
        Set<String> msgList = new LinkedHashSet<>();
        while (matcher.find()) {
            msgList.add(matcher.group(1));
        }
        return msgList;
    }

    @Override
    public ApiResponse executeException(ApiRequestContext apiRequestContext, Exception e) {
        if (e instanceof GenericException) {
            GenericException genericException = (GenericException) e;
            String exceptionClass = genericException.getExceptionClass();
            if (exceptionClass.contains(CONSTRAINT_VIOLATION_EXCEPTION)) {
                String exceptionMessage = genericException.getExceptionMessage();
                // 参数校验:Failed to validate service: com.gitee.sop.storyweb.open.StoryService, method: save, cause: [ConstraintViolationImpl{interpolatedMessage='故事名称必填', propertyPath=productName, rootBeanClass=class com.gitee.sop.storyweb.open.req.StorySaveDTO, messageTemplate='故事名称必填'}]
                Set<String> msgs = findErrorMsg(exceptionMessage);
                return ApiResponse.error(ErrorEnum.ISV_ERROR_PARAMETER, apiRequestContext.getLocale(), String.join(",", msgs));
            }
            if (exceptionClass.contains(OPEN_EXCEPTION)) {
                String exceptionMessage = genericException.getExceptionMessage();

                Matcher matcher = PATTERN.matcher(exceptionMessage);
                String errorMsg;
                if (matcher.find()) {
                    errorMsg = matcher.group(1);
                    // isv.common-error@@系统错误@@
                    String[] msgAr = errorMsg.split("@@");
                    String subCode = msgAr[0];
                    String subMsg = msgAr[1];
                    String solution = msgAr.length == 3 ? msgAr[2] : null;
                    return ApiResponse.error(
                            ErrorEnum.BIZ_ERROR,
                            apiRequestContext.getLocale(),
                            subCode,
                            subMsg,
                            solution
                    );
                } else {
                    ErrorEnum bizError = ErrorEnum.ISP_SERVICE_UNKNOWN_ERROR;
                    return ApiResponse.error(bizError.getError(apiRequestContext.getLocale()));
                }

            }
        } else if (e instanceof ApiException) {
            return ApiResponse.error(((ApiException) e).getError());
        }
        return ApiResponse.error(ErrorEnum.ISP_SERVICE_UNKNOWN_ERROR, apiRequestContext.getLocale());
    }
}
