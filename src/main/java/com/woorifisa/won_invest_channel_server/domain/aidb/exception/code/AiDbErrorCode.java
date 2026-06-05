package com.woorifisa.won_invest_channel_server.domain.aidb.exception.code;

import com.woorifisa.won_invest_channel_server.global.exception.code.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AiDbErrorCode implements ErrorCode {

    UNSUPPORTED_QUERY_TYPE(HttpStatus.BAD_REQUEST, "CHAT_400_003", "지원하지 않는 queryType입니다."),
    QUERY_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_404_001", "조회 결과가 없습니다."),
    MYSQL_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_500_001", "MySQL 조회 중 오류가 발생했습니다."),
    MYSQL_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "CHAT_503_001", "MySQL 연결에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    AiDbErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
