package com.woorifisa.won_invest_channel_server.domain.account.exception.code;

import com.woorifisa.won_invest_channel_server.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum InvestAccountErrorCode implements ErrorCode {

    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "INVST_404_001", "증권계좌 정보를 찾을 수 없습니다."),
    INVALID_ACCOUNT_STATUS(HttpStatus.BAD_REQUEST, "INVST_400_001", "사용할 수 없는 증권계좌 상태입니다."),
    NOT_ACCOUNT_OWNER(HttpStatus.FORBIDDEN, "INVST_403_001", "해당 증권계좌에 접근할 권한이 없습니다."),
    ALREADY_LINKED(HttpStatus.CONFLICT, "INVST_409_001", "이미 연결된 증권계좌입니다.");

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
