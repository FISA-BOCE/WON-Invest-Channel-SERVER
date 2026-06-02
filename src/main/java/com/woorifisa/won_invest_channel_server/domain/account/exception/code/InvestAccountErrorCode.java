package com.woorifisa.won_invest_channel_server.domain.account.exception.code;

import com.woorifisa.won_invest_channel_server.global.exception.code.ErrorCode;
import org.springframework.http.HttpStatus;

public enum InvestAccountErrorCode implements ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVEST_400_001", "입력값 형식이 올바르지 않습니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "INVEST_400_002", "비밀번호가 일치하지 않습니다."),
    ACCOUNT_ALREADY_CONNECTED(HttpStatus.BAD_REQUEST, "INVEST_400_003", "이미 연결된 증권계좌가 존재합니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "INVEST_400_004", "필수 약관에 동의하지 않았습니다."),
    INVALID_ACCOUNT_STATUS(HttpStatus.BAD_REQUEST, "INVEST_400_005", "사용할 수 없는 증권계좌 상태입니다."),

    NOT_ACCOUNT_OWNER(HttpStatus.FORBIDDEN, "INVEST_403_001", "해당 증권계좌에 접근할 권한이 없습니다."),

    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "INVEST_404_001", "증권계좌 정보를 찾을 수 없습니다."),
    USER_MAPPING_NOT_FOUND(HttpStatus.NOT_FOUND, "INVEST_404_002", "공통 사용자 매핑 정보를 찾을 수 없습니다. 먼저 회원 연동 정보를 확인해 주세요."),

    ALREADY_LINKED(HttpStatus.CONFLICT, "INVEST_409_001", "이미 연결된 증권계좌입니다."),

    CORE_ACCOUNT_PERSISTENCE_FAILED(HttpStatus.BAD_GATEWAY, "INVEST_502_001", "증권계좌 원본 정보 저장 확인에 실패했습니다.");

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
