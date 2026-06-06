package com.woorifisa.won_invest_channel_server.domain.etf.exception.code;

import com.woorifisa.won_invest_channel_server.global.exception.code.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum EtfErrorCode implements ErrorCode {

    ETF_CANDIDATE_EMPTY(HttpStatus.BAD_REQUEST, "ETF_400_001", "ETF 후보 정보가 없습니다."),
    ETF_PRODUCT_EMPTY(HttpStatus.BAD_REQUEST, "ETF_400_002", "ETF 상품 정보가 없습니다."),
    CORE_ETF_UPSERT_REQUEST_EMPTY(HttpStatus.BAD_REQUEST, "ETF_400_003", "Core ETF 상품 동기화 요청 정보가 없습니다."),
    ETF_NOT_ELIGIBLE(HttpStatus.BAD_REQUEST, "ETF_400_006", "서비스 제공 불가 ETF입니다."),
    ETF_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "ETF_404_001", "ETF 상품을 찾을 수 없습니다."),
    CORE_ETF_ID_EMPTY(HttpStatus.BAD_GATEWAY, "ETF_502_001", "Core ETF ID가 없습니다."),

    KIS_PRODUCT_TYPE_CODE_EMPTY(HttpStatus.BAD_REQUEST, "ETF_400_004", "KIS 상품유형코드가 없습니다."),
    KIS_PRODUCT_NUMBER_EMPTY(HttpStatus.BAD_REQUEST, "ETF_400_005", "KIS 상품번호가 없습니다."),
    KIS_PRODUCT_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "ETF_502_002", "KIS ETF 상품기본정보 응답이 없습니다."),
    KIS_PRODUCT_OUTPUT_EMPTY(HttpStatus.BAD_GATEWAY, "ETF_502_003", "KIS ETF 상품기본정보 상세 응답이 없습니다."),
    KIS_PRODUCT_RESPONSE_FAILED(HttpStatus.BAD_GATEWAY, "ETF_502_009", "KIS ETF 상품기본정보 API 응답이 실패했습니다."),
    KIS_APP_KEY_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "ETF_500_001", "KIS appKey 설정이 없습니다."),
    KIS_APP_SECRET_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "ETF_500_002", "KIS appSecret 설정이 없습니다."),
    KIS_ACCESS_TOKEN_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "ETF_500_003", "KIS Access Token 설정이 없습니다."),
    KIS_PRODUCT_API_FAILED(HttpStatus.BAD_GATEWAY, "ETF_502_004", "KIS ETF 상품기본정보 API 호출에 실패했습니다."),

    CORE_SERVICE_ID_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "ETF_500_004", "Core 내부 API Service ID 설정이 없습니다."),
    CORE_INTERNAL_API_KEY_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "ETF_500_005", "Core 내부 API Key 설정이 없습니다."),
    CORE_ETF_UPSERT_API_FAILED(HttpStatus.BAD_GATEWAY, "ETF_502_005", "Core ETF 상품 동기화 API 호출에 실패했습니다."),
    CORE_ETF_UPSERT_RESPONSE_EMPTY(HttpStatus.BAD_GATEWAY, "ETF_502_006", "Core ETF 상품 동기화 API 응답이 없습니다."),
    CORE_ETF_UPSERT_RESPONSE_FAILED(HttpStatus.BAD_GATEWAY, "ETF_502_007", "Core ETF 상품 동기화 API 응답이 실패했습니다."),
    CORE_ETF_UPSERT_RESPONSE_DATA_EMPTY(HttpStatus.BAD_GATEWAY, "ETF_502_008", "Core ETF 상품 동기화 API 응답 data가 없습니다."),
    CORE_ETF_UPSERT_RESPONSE_ETF_ID_EMPTY(HttpStatus.BAD_GATEWAY, "ETF_502_010", "Core ETF 상품 동기화 API 응답 etfId가 없습니다."),

    ETF_SYNC_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ETF_500_006", "동기화 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    EtfErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
