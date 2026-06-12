package com.woorifisa.won_invest_channel_server.global.exception.handler;

import com.woorifisa.won_invest_channel_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_channel_server.global.exception.code.ErrorCode;
import com.woorifisa.won_invest_channel_server.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("business exception: code={}, method={}, uri={}", errorCode.getCode(), request.getMethod(), request.getRequestURI());
        return toResponseEntity(errorCode);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            ServletRequestBindingException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e, HttpServletRequest request) {
        logBadRequest(e, request);
        return toResponseEntity(CommonErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("method not supported: method={}, uri={}", request.getMethod(), request.getRequestURI());
        return toResponseEntity(CommonErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("unexpected exception: method={}, uri={}", request.getMethod(), request.getRequestURI(), e);
        return toResponseEntity(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    // -------------------------------------------------------------------------
    // private helpers
    // -------------------------------------------------------------------------

    private void logBadRequest(Exception e, HttpServletRequest request) {
        if (e instanceof MethodArgumentNotValidException validationEx) {
            String fields = validationEx.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ":" + fe.getCode())
                    .collect(Collectors.joining(", "));
            log.warn("bad request: method={}, uri={}, fields={}", request.getMethod(), request.getRequestURI(), fields);

        } else if (e instanceof ConstraintViolationException constraintEx) {
            String violations = constraintEx.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ":" + v.getMessageTemplate())
                    .collect(Collectors.joining(", "));
            log.warn("bad request: method={}, uri={}, violations={}", request.getMethod(), request.getRequestURI(), violations);

        } else if (e instanceof HttpMessageNotReadableException notReadableEx) {
            Throwable rootCause = notReadableEx.getMostSpecificCause();
            String rootCauseType = rootCause == null ? "unknown" : rootCause.getClass().getSimpleName();
            log.warn("bad request: method={}, uri={}, rootCause={}", request.getMethod(), request.getRequestURI(), rootCauseType);

        } else {
            log.warn("bad request: method={}, uri={}", request.getMethod(), request.getRequestURI());
        }
    }

    private ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode));
    }
}
