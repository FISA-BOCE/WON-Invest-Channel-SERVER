package com.woorifisa.won_invest_channel_server.domain.sweep.exception.code;

import com.woorifisa.won_invest_channel_server.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum SweepErrorCode implements ErrorCode {

    INVALID_SWEEP_EVENT_TYPE(HttpStatus.BAD_REQUEST, "SWEEP_400_001", "지원하지 않는 스윕 이벤트 유형입니다."),
    INVALID_SWEEP_EVENT_PAYLOAD(HttpStatus.BAD_REQUEST, "SWEEP_400_002", "스윕 이벤트 payload가 올바르지 않습니다."),
    INVALID_SWEEP_AMOUNT(HttpStatus.BAD_REQUEST, "SWEEP_400_003", "스윕 금액이 올바르지 않습니다."),

    SWEEP_OUTBOX_RETRY_NOT_ALLOWED(HttpStatus.CONFLICT, "SWEEP_409_001", "재처리할 수 없는 증권 Outbox 이벤트입니다."),

    SWEEP_CORE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SWEEP_503_001", "증권 Core 연동에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    SweepErrorCode(HttpStatus httpStatus, String code, String message) {
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
