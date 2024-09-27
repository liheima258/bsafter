package com.example.bsafter.exception;

import com.example.bsafter.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 功能：全局异常类
 * 日期：2024/4/719:39
 */

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(ServiceException.class)
    public Result serviceException(ServiceException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

}