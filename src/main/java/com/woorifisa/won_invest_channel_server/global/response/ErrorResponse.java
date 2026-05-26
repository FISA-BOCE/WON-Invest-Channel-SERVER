package com.woorifisa.won_invest_channel_server.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.woorifisa.won_invest_channel_server.global.exception.code.ErrorCode;
import org.springframework.http.ResponseEntity;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String code,
        String message,
        List<FieldErrorDetail> errors
) {
    public record FieldErrorDetail(String field, String message) {}

    public static ResponseEntity<ErrorResponse> of(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(
                        errorCode.getHttpStatus().value(),
                        errorCode.getCode(),
                        errorCode.getMessage(),
                        null
                ));
    }

    public static ResponseEntity<ErrorResponse> ofValidation(ErrorCode errorCode, List<FieldErrorDetail> errors) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(
                        errorCode.getHttpStatus().value(),
                        errorCode.getCode(),
                        errorCode.getMessage(),
                        errors
                ));
    }
}