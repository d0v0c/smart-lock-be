package ie.tcd.smartlock.handler;

import ie.tcd.smartlock.utils.BusinessException;
import ie.tcd.smartlock.utils.Result;
import ie.tcd.smartlock.utils.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// 返回的数据直接写入 HTTP Response Body
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // 自定义运行时异常
    @ExceptionHandler(BusinessException.class)
    public Result<Void> businessException(BusinessException e) {
        log.error("businessException: {}", e.getMessage());
        return Result.fail(e.getStatusCode(), e.getMessage());
    }

    // POST请求中 @RequestBody 校验失败时调用
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> argumentValidException(MethodArgumentNotValidException e) {
        String message = e.getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.error("表单数据校验失败 argumentValidException: {}", message);
        return Result.fail(StatusCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> exception(Exception e) {
        log.error("exception: {}", e.getMessage(), e);
        return Result.fail(StatusCode.SYSTEM_ERROR);
    }
}