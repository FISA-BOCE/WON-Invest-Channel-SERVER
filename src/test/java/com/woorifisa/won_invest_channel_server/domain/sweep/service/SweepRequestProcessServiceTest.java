package com.woorifisa.won_invest_channel_server.domain.sweep.service;

import com.woorifisa.won_invest_channel_server.domain.sweep.dto.event.SweepRequestedEvent;
import com.woorifisa.won_invest_channel_server.domain.sweep.dto.response.InvestCoreSweepExecutionResponse;
import com.woorifisa.won_invest_channel_server.domain.sweep.exception.code.SweepErrorCode;
import com.woorifisa.won_invest_channel_server.domain.sweep.external.InvestCoreSweepExecutionApi;
import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepExecutionStatus;
import com.woorifisa.won_invest_channel_server.global.exception.handler.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SweepRequestProcessServiceTest {

    private SweepInboxService inboxService;
    private SweepRequestSuccessService successService;
    private InvestCoreSweepExecutionApi investCoreApi;
    private SweepRequestProcessService processService;

    @BeforeEach
    void setUp() {
        inboxService = mock(SweepInboxService.class);
        successService = mock(SweepRequestSuccessService.class);
        investCoreApi = mock(InvestCoreSweepExecutionApi.class);
        processService = new SweepRequestProcessService(inboxService, investCoreApi, successService);
    }

    @Test
    @DisplayName("Core가 COMPLETED를 반환하면 성공 결과 outbox를 저장하고 inbox를 처리 완료한다")
    void processCompleted() {
        SweepRequestedEvent event = validEvent();
        InvestCoreSweepExecutionResponse response =
                new InvestCoreSweepExecutionResponse(10L, event.idempotencyKey(), SweepExecutionStatus.COMPLETED, null, null);

        when(investCoreApi.executeSweep(any())).thenReturn(response);

        processService.process(1L, event);

        verify(successService).saveResultAndMarkProcessed(1L, event, response);
        verify(inboxService, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("Core가 FAILED를 반환하면 실패 결과 outbox를 저장하고 inbox를 처리 완료한다")
    void processBusinessFailed() {
        SweepRequestedEvent event = validEvent();
        InvestCoreSweepExecutionResponse response =
                new InvestCoreSweepExecutionResponse(10L, event.idempotencyKey(), SweepExecutionStatus.FAILED,
                        "ETF_NOT_FOUND", "ETF 상품을 찾을 수 없습니다.");

        when(investCoreApi.executeSweep(any())).thenReturn(response);

        processService.process(1L, event);

        verify(successService).saveResultAndMarkProcessed(1L, event, response);
        verify(inboxService, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("Core 응답이 null이면 시스템 실패로 보고 outbox를 저장하지 않는다")
    void processNullCoreResponse() {
        SweepRequestedEvent event = validEvent();

        when(investCoreApi.executeSweep(any())).thenReturn(null);

        assertThatThrownBy(() -> processService.process(1L, event))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SweepErrorCode.SWEEP_CORE_UNAVAILABLE);

        verify(successService, never()).saveResultAndMarkProcessed(any(), any(), any());
        verify(inboxService).markFailed(eq(1L), any());
    }

    @Test
    @DisplayName("Core 응답 status가 null이면 시스템 실패로 보고 outbox를 저장하지 않는다")
    void processNullCoreStatus() {
        SweepRequestedEvent event = validEvent();
        InvestCoreSweepExecutionResponse response =
                new InvestCoreSweepExecutionResponse(10L, event.idempotencyKey(), null, null, null);

        when(investCoreApi.executeSweep(any())).thenReturn(response);

        assertThatThrownBy(() -> processService.process(1L, event))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SweepErrorCode.SWEEP_CORE_UNAVAILABLE);

        verify(successService, never()).saveResultAndMarkProcessed(any(), any(), any());
        verify(inboxService).markFailed(eq(1L), any());
    }

    @Test
    @DisplayName("idempotencyKey가 없으면 Core를 호출하지 않는다")
    void validateMissingIdempotencyKey() {
        SweepRequestedEvent event = new SweepRequestedEvent(
                "CARD-SWEEP-1",
                "SWEEP_REQUESTED",
                "corr-1",
                null,
                1L,
                "user-uuid",
                "card-user-uuid",
                1L,
                1L,
                "2026-05",
                1000L,
                1000L,
                100L,
                LocalDateTime.now()
        );

        assertThatThrownBy(() -> processService.process(1L, event))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(SweepErrorCode.INVALID_SWEEP_EVENT_PAYLOAD);

        verify(successService, never()).saveResultAndMarkProcessed(any(), any(), any());
        verify(inboxService).markFailed(eq(1L), any());
    }

    private SweepRequestedEvent validEvent() {
        return new SweepRequestedEvent(
                "CARD-SWEEP-1",
                "SWEEP_REQUESTED",
                "corr-1",
                "SWEEP:POINT_LEDGER:1",
                1L,
                "user-uuid",
                "card-user-uuid",
                1L,
                1L,
                "2026-05",
                1000L,
                1000L,
                100L,
                LocalDateTime.now()
        );
    }
}
