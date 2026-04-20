package com.example.newcourseregistrationsystem.config;

import com.example.oldcommonbase.exception.BusinessException;
import com.example.oldcommonbase.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 处理业务异常，返回统一的错误响应
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    // 处理参数校验异常，提取具体的错误信息并返回给前端
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleValidation(Exception e) {
        String msg = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException m) {
            if (m.getBindingResult().getFieldError() != null) {
                msg = m.getBindingResult().getFieldError().getDefaultMessage();
            }
        } else if (e instanceof BindException b && b.getBindingResult().getFieldError() != null) {
            msg = b.getBindingResult().getFieldError().getDefaultMessage();
        }
        return Result.fail(msg);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBadRequest(Exception e) {
        log.warn("请求参数异常: {}", e.getMessage());
        return Result.fail("请求参数有误，请检查后重试");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("数据冲突: {}", e.getMessage());
        return Result.fail("数据已存在，请勿重复提交");
    }

    // 处理其他未捕获的异常，记录错误日志并返回通用错误消息
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleOther(Exception e) {
        log.error("未处理异常", e);
        return Result.fail("系统繁忙，请稍后再试");
    }
}
