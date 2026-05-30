package com.woorifisa.won_invest_channel_server.domain.etf.exception;

import com.woorifisa.won_invest_channel_server.global.exception.code.ErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;

public class EtfSyncException extends BusinessException {

    public EtfSyncException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EtfSyncException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public EtfSyncException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
