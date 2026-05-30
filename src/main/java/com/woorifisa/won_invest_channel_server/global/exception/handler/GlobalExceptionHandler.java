package com.woorifisa.won_invest_channel_server.global.exception.handler;

import com.woorifisa.won_invest_channel_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.code.ErrorCode;
import com.woorifisa.won_invest_channel_server.global.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("business exception: code={}, type={}", errorCode.getCode(), e.getClass().getSimpleName());
        return toResponseEntity(errorCode);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            ServletRequestBindingException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        logBadRequest(e);
        return toResponseEntity(CommonErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("method not supported: {}", e.getMessage());
        return toResponseEntity(CommonErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("unexpected exception", e);
        return toResponseEntity(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    // -------------------------------------------------------------------------
    // private helpers
    // -------------------------------------------------------------------------

    private void logBadRequest(Exception e) {
        if (e instanceof MethodArgumentNotValidException validationEx) {
            String fields = validationEx.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ":" + fe.getCode())
                    .collect(Collectors.joining(", "));
            log.warn("bad request: type={}, fields={}", e.getClass().getSimpleName(), fields, e);

        } else if (e instanceof ConstraintViolationException constraintEx) {
            String violations = constraintEx.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ":" + v.getMessageTemplate())
                    .collect(Collectors.joining(", "));
            log.warn("bad request: type={}, violations={}", e.getClass().getSimpleName(), violations, e);

        } else if (e instanceof HttpMessageNotReadableException notReadableEx) {
            Throwable rootCause = notReadableEx.getMostSpecificCause();
            String rootCauseType = rootCause == null ? "unknown" : rootCause.getClass().getSimpleName();
            log.warn("bad request: type={}, rootCause={}", e.getClass().getSimpleName(), rootCauseType, e);

        } else {
            log.warn("bad request: type={}", e.getClass().getSimpleName(), e);
        }
    }

    private ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode));
    }
}
