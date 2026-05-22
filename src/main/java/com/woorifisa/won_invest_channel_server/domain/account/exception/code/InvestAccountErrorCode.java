package com.woorifisa.won_invest_channel_server.domain.account.exception.code;

import com.woorifisa.won_invest_channel_server.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum InvestAccountErrorCode implements ErrorCode {

    ALREADY_LINKED(HttpStatus.CONFLICT, "INVEST_409_001", "이미 연결된 투자 계좌가 있습니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "INVEST_404_001", "투자 계좌를 찾을 수 없습니다."),
    NOT_ACCOUNT_OWNER(HttpStatus.FORBIDDEN, "INVEST_403_001", "해당 계좌의 소유자가 아닙니다."),
    INVALID_ACCOUNT_STATUS(HttpStatus.BAD_REQUEST, "INVEST_400_001", "유효하지 않은 계좌 상태입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    InvestAccountErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
