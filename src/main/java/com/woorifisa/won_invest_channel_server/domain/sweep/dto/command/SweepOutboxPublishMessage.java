package com.woorifisa.won_invest_channel_server.domain.sweep.dto.command;

import com.woorifisa.won_invest_channel_server.domain.sweep.model.enums.SweepEventType;

public record SweepOutboxPublishMessage(
        Long outboxEventId,
        String eventId,
        SweepEventType eventType,
        Long sweepRequestId,
        String correlationId,
        String idempotencyKey,
        String payload
) {
}
