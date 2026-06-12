package com.woorifisa.won_invest_channel_server.domain.invest.exception.code;

import com.woorifisa.won_invest_channel_server.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum InvestErrorCode implements ErrorCode {

    ACCOUNT_NOT_OWNER(HttpStatus.FORBIDDEN, "INVEST_403_001", "본인 명의 계좌가 아닙니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "INVEST_404_001", "증권계좌를 찾을 수 없습니다."),
    INVALID_ACCOUNT_STATUS(HttpStatus.BAD_REQUEST, "INVEST_400_001", "정상 상태의 증권계좌가 아닙니다."),
    INVALID_AUTO_INVEST_EXECUTION_QUERY(HttpStatus.BAD_REQUEST, "INVEST_400_002", "자동 투자 체결 이력 조회 조건이 올바르지 않습니다."),
    HOLDINGS_NOT_FOUND(HttpStatus.NOT_FOUND, "INVEST_404_002", "보유 ETF가 없습니다."),
    INTERNAL_QUERY_FAILED(HttpStatus.BAD_GATEWAY, "INVEST_502_001", "ETF 보유 정보 조회에 실패했습니다."),
    AUTO_INVEST_EXECUTION_HISTORY_QUERY_FAILED(HttpStatus.BAD_GATEWAY, "INVEST_502_002", "자동 투자 체결 이력 조회에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    InvestErrorCode(HttpStatus httpStatus, String code, String message) {
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
