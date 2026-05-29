package com.woorifisa.won_invest_channel_server.domain.sweep.dto.event;

import java.time.LocalDateTime;

public record SweepRequestedEvent(
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
}
