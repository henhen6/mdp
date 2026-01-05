package top.mddata.gateway.sop.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.mddata.gateway.sop.message.ErrorEnum;
import top.mddata.gateway.sop.response.ApiResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestControllerAdvice
public class ControllerGlobalExceptionHandler {

    // 未知异常
    @ExceptionHandler(value = Exception.class)
    public ApiResponse globalExceptionHandler(Exception e, HttpServletRequest request) {
        log.error("系统出错", e);
        return ApiResponse.error(ErrorEnum.ISP_UNKNOWN_ERROR, request.getLocale());
    }

    /**
     * 处理jsr303的字段校验异常，也可以自定义注解校验
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ApiResponse.error(ErrorEnum.ISV_ERROR_PARAMETER, request.getLocale(), String.join(",", errors.values()));
    }

    /**
     * 验证异常
     *
     * @param e e
     */
    @ExceptionHandler(ValidationException.class)
    public ApiResponse validationException(ValidationException e, HttpServletRequest request) {
        List<String> msgList = new ArrayList<>();
        for (ConstraintViolation<?> constraintViolation : ((ConstraintViolationException) e).getConstraintViolations()) {
            msgList.add(constraintViolation.getMessage());
        }
        return ApiResponse.error(ErrorEnum.ISV_ERROR_PARAMETER, request.getLocale(), String.join(",", msgList));
    }

    /**
     * 参数绑定异常，GET请求参数校验
     *
     * @param e e
     */
    @ExceptionHandler(BindException.class)
    public ApiResponse bandException(BindException e, HttpServletRequest request) {
        List<FieldError> fieldErrors = e.getFieldErrors();
        List<String> msgList = new ArrayList<>();
        for (FieldError fieldError : fieldErrors) {
            msgList.add(fieldError.getDefaultMessage());
        }
        return ApiResponse.error(ErrorEnum.ISV_ERROR_PARAMETER, request.getLocale(), String.join(",", msgList));
    }
}
