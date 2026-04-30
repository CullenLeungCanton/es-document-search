package com.example.es1.common.exception;

import com.example.es1.common.result.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{
    private final Integer code;
    private final String message;

    public BusinessException(String message) {
        super(message);
        this.code = 1001;
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }
}
