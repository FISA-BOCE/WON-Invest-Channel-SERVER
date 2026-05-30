package com.woorifisa.won_invest_channel_server.global.exception.handler;

import com.woorifisa.won_invest_channel_server.global.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message == null || message.isBlank() ? errorCode.getMessage() : message);
        this.errorCode = errorCode;
    }
}
