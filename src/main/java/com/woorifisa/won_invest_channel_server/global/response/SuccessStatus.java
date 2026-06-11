package com.woorifisa.won_invest_channel_server.global.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessStatus {

    OK(HttpStatus.OK, "COM_200_001", "OK"),
    CREATED(HttpStatus.CREATED, "COM_201_001", "CREATED"),
    NO_CONTENT(HttpStatus.NO_CONTENT, "COM_204_001", "NO_CONTENT"),

    ACCOUNT_CREATED(HttpStatus.CREATED, "INVEST_201_001", "증권계좌 개설이 완료되었습니다."),
    ACCOUNT_LINKED(HttpStatus.OK, "INVEST_200_001", "투자 계좌 연결이 완료되었습니다."),
    INVEST_ACCOUNT_LIST_FOUND(HttpStatus.OK, "INVEST_200_005", "증권 계좌 목록 조회가 완료되었습니다."),
    INVEST_ACCOUNT_SUMMARY_UPSERTED(HttpStatus.OK, "INVEST_200_006", "증권 계좌 summary 동기화가 완료되었습니다."),
    INVEST_ACCOUNT_ETF_FOUND(HttpStatus.OK, "INVEST_200_007", "보유 ETF 조회가 완료되었습니다."),
    INVEST_ACCOUNT_DETAIL_FOUND(HttpStatus.OK, "INVEST_200_008", "증권 계좌 상세 조회가 완료되었습니다."),
    INVEST_MAIN_FOUND(HttpStatus.OK, "INVEST_200_009", "투자 자산 요약 조회가 완료되었습니다."),

    CORE_ETF_PRODUCT_SYNCED(HttpStatus.OK, "ETF_200_001", "ETF 상품 마스터 동기화가 완료되었습니다."),
    ETF_PRODUCT_SYNC_SUCCESS(HttpStatus.OK, "INVEST_200_002", "ETF 상품 동기화가 완료되었습니다."),
    ETF_PRODUCT_LIST_FOUND(HttpStatus.OK, "INVEST_200_003", "ETF 상품 목록 조회가 완료되었습니다."),
    ETF_PRODUCT_DETAIL_FOUND(HttpStatus.OK, "INVEST_200_004", "ETF 상품 상세 조회가 완료되었습니다."),

    ADMIN_INVEST_OUTBOX_EVENTS_FOUND(HttpStatus.OK, "ADMIN_200_001", "관리자 증권 Outbox 이벤트 목록 조회가 완료되었습니다."),
    ADMIN_INVEST_OUTBOX_EVENT_DETAIL_FOUND(HttpStatus.OK, "ADMIN_200_002", "관리자 증권 Outbox 이벤트 상세 조회가 완료되었습니다."),
    ADMIN_INVEST_OUTBOX_EVENT_RETRY_REQUESTED(HttpStatus.OK, "ADMIN_200_003", "관리자 증권 Outbox 이벤트 재처리 요청이 완료되었습니다."),
    ADMIN_INVEST_INBOX_EVENTS_FOUND(HttpStatus.OK, "ADMIN_200_004", "관리자 증권 Inbox 이벤트 목록 조회가 완료되었습니다."),
    ADMIN_INVEST_INBOX_EVENT_DETAIL_FOUND(HttpStatus.OK, "ADMIN_200_005", "관리자 증권 Inbox 이벤트 상세 조회가 완료되었습니다."),
    ADMIN_AUTO_INVEST_EXECUTIONS_FOUND(HttpStatus.OK, "ADMIN_200_006", "관리자 자동투자 실행 현황 조회가 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    SuccessStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
