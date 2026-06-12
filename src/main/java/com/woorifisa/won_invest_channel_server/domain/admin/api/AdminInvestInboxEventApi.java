package com.woorifisa.won_invest_channel_server.domain.admin.api;

import com.woorifisa.won_invest_channel_server.domain.admin.dto.response.AdminInvestInboxEventItemResponse;
import com.woorifisa.won_invest_channel_server.domain.admin.dto.response.AdminInvestInboxEventListResponse;
import com.woorifisa.won_invest_channel_server.domain.admin.service.AdminInvestInboxEventService;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;
import com.woorifisa.won_invest_channel_server.global.response.ApiResponse;
import com.woorifisa.won_invest_channel_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/invest/inbox-events")
@Tag(name = "Admin Invest Inbox Event API", description = "관리자 대시보드의 Invest Channel Inbox 이벤트를 조회하는 API")
public class AdminInvestInboxEventApi {

    private final AdminInvestInboxEventService adminInvestInboxEventService;

    @Operation(
            summary = "증권 Inbox 이벤트 목록 조회",
            description = "Invest Channel의 invest_chn_inbox_event 테이블 기준으로 카드망에서 수신한 스윕 요청 이벤트 목록과 상태별 요약 정보를 조회합니다. 시스템, 처리 상태, 이벤트 타입, 스윕 요청 ID로 필터링할 수 있으며 페이지네이션을 지원합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<AdminInvestInboxEventListResponse>> getInboxEvents(
            @RequestParam(required = false) String systemType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) SweepEventType eventType,
            @RequestParam(required = false) Long sweepRequestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AdminInvestInboxEventListResponse response = adminInvestInboxEventService.getInboxEvents(
                systemType,
                status,
                eventType,
                sweepRequestId,
                page,
                size
        );

        return ResponseEntity
                .status(SuccessStatus.ADMIN_INVEST_INBOX_EVENTS_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.ADMIN_INVEST_INBOX_EVENTS_FOUND, response));
    }

    @Operation(
            summary = "증권 Inbox 이벤트 상세 조회",
            description = "Inbox 이벤트 ID로 Invest Channel의 이벤트 수신 상태, 재시도 횟수, 마지막 오류 메시지, 수신/처리 시각을 조회합니다."
    )
    @GetMapping("/{inboxEventId}")
    public ResponseEntity<ApiResponse<AdminInvestInboxEventItemResponse>> getInboxEvent(
            @PathVariable Long inboxEventId
    ) {
        AdminInvestInboxEventItemResponse response = adminInvestInboxEventService.getInboxEvent(inboxEventId);

        return ResponseEntity
                .status(SuccessStatus.ADMIN_INVEST_INBOX_EVENT_DETAIL_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.ADMIN_INVEST_INBOX_EVENT_DETAIL_FOUND, response));
    }
}
