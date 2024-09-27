package com.example.bsafter.exception;

import lombok.Getter;

/**
 * 功能：异常类
 * 日期：2024/4/719:41
 */
@Getter
public class ServiceException extends RuntimeException {

    private String code;

    public ServiceException(String msg) {
        super(msg);
        this.code = "500";
    }

    public ServiceException(String code, String msg) {
        super(msg);
        this.code = code;
    }

}
