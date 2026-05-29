package com.woorifisa.won_invest_channel_server.domain.sweep.dto.request;

import com.woorifisa.won_invest_channel_server.domain.sweep.dto.event.SweepRequestedEvent;

import java.time.LocalDateTime;

public record InvestCoreSweepExecutionRequest(
        String eventId,
        String eventType,
        String correlationId,
        String idempotencyKey,
        Long sweepRequestId,
        String userUuid,
        String cardUserUuid,
        Long performanceId,
        Long pointLedgerId,
        String baseMonth,
        Long pointAmount,
        Long krwAmount,
        Long etfId,
        LocalDateTime requestedAt
) {
    public static InvestCoreSweepExecutionRequest from(SweepRequestedEvent event) {
        return new InvestCoreSweepExecutionRequest(
                event.eventId(),
                event.eventType(),
                event.correlationId(),
                event.idempotencyKey(),
                event.sweepRequestId(),
                event.userUuid(),
                event.cardUserUuid(),
                event.performanceId(),
                event.pointLedgerId(),
                event.baseMonth(),
                event.pointAmount(),
                event.krwAmount(),
                event.etfId(),
                event.requestedAt()
        );
    }
}
